package kr.ac.pusan.feedback.auth;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 인증 진입 경로와 개발 환경 확인. */
@Hidden
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    static final String LOGIN_INTENT = "feedback.login.intent";
    static final String LOGIN_SITE = "feedback.login.site";
    private final ProjectRepository projects;
    private final ObjectProvider<ClientRegistrationRepository> registrations;
    private final Environment environment;
    public AuthController(ObjectProvider<ClientRegistrationRepository> registrations, Environment environment, ProjectRepository projects) {
        this.registrations = registrations;
        this.environment = environment;
        this.projects = projects;
    }
    private boolean googleEnabled() {
        var repository = registrations.getIfAvailable();
        return repository != null && repository.findByRegistrationId("google") != null;
    }
    @GetMapping("/config")
    public AuthConfig config() {
        return new AuthConfig(googleEnabled(), environment.acceptsProfiles(Profiles.of("dev")));
    }
    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }
    @GetMapping("/login")
    public void login(@RequestParam(defaultValue = "user") String intent,
                      @RequestParam(required = false) String site,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Set.of("user", "admin").contains(intent) || (site != null && projects.findByCode(site).isEmpty()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "로그인 경로가 올바르지 않습니다.");
        if (!googleEnabled())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "구글 로그인 설정이 아직 완료되지 않았습니다.");
        var session = request.getSession();
        session.setAttribute(LOGIN_INTENT, intent);
        if (site == null) session.removeAttribute(LOGIN_SITE);
        else session.setAttribute(LOGIN_SITE, site);
        response.sendRedirect("/oauth2/authorization/google");
    }
    public record AuthConfig(boolean googleEnabled, boolean previewEnabled) {}
    public record CsrfResponse(String headerName, String token) {}
}
