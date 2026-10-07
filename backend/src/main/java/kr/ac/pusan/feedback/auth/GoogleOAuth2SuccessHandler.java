package kr.ac.pusan.feedback.auth;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.UserRepository;

/** 구글이 검증한 계정을 기존 @CurrentUser 세션 계약으로 변환한다. */
@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository users;
    private final String bootstrapEmail;
    private final String frontendUrl;
    public GoogleOAuth2SuccessHandler(UserRepository users,
            @Value("${app.bootstrap-developer-email:}") String bootstrapEmail,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.users = users;
        this.bootstrapEmail = bootstrapEmail;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }
    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User google = (OAuth2User) authentication.getPrincipal();
        String sub = google.getAttribute("sub");
        String email = google.getAttribute("email");
        if (sub == null || sub.isBlank() || email == null || email.isBlank()
                || !Boolean.TRUE.equals(google.getAttribute("email_verified"))) {
            reject(request, response, "invalid_google_account");
            return;
        }
        var session = request.getSession(false);
        boolean admin = session != null && "admin".equals(session.getAttribute(AuthController.LOGIN_INTENT));
        String site = session == null ? null : (String) session.getAttribute(AuthController.LOGIN_SITE);
        User user = users.findByGoogleSub(sub).orElseGet(() -> users.findByEmail(email).orElse(null));
        if (user != null && (user.getStatus() == UserStatus.DISABLED
                || (user.getGoogleSub() != null && !sub.equals(user.getGoogleSub())))) {
            reject(request, response, user.getStatus() == UserStatus.DISABLED ? "disabled" : "account_conflict");
            return;
        }
        if (user == null) {
            String name = google.getAttribute("name");
            boolean bootstrap = !bootstrapEmail.isBlank() && bootstrapEmail.equalsIgnoreCase(email);
            user = User.builder().email(email).name(name == null || name.isBlank() ? email : name)
                    .googleSub(sub).role(bootstrap ? Role.DEVELOPER : Role.USER)
                    .status(bootstrap || !admin ? UserStatus.ACTIVE : UserStatus.PENDING)
                    .createdAt(LocalDateTime.now()).build();
        } else if (user.getGoogleSub() == null) user.linkGoogleSub(sub);
        users.saveAndFlush(user);
        var principal = AppUserPrincipal.from(user);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(AuthController.LOGIN_INTENT);
            session.removeAttribute(AuthController.LOGIN_SITE);
        }
        String target = user.getStatus() == UserStatus.PENDING ? "/admin/pending"
                : user.getRole() != Role.USER ? "/admin" : site != null ? "/write?site=" + java.net.URLEncoder.encode(site, java.nio.charset.StandardCharsets.UTF_8) : "/home";
        response.sendRedirect(frontendUrl + target);
    }
    private void reject(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        response.sendRedirect(frontendUrl + "/?authError=" + reason);
    }
}
