package kr.ac.pusan.feedback.api.dev;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import kr.ac.pusan.feedback.api.auth.CurrentUserResponse;
import kr.ac.pusan.feedback.domain.Role;
import kr.ac.pusan.feedback.security.CurrentUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Profile("dev")
@RestController
@RequestMapping("/api/dev")
public class DemoLoginController {

	private static final Map<String, CurrentUser> DEMO_USERS = Map.of(
			"dev@demo.local", new CurrentUser(1L, "dev@demo.local", "데모 개발자", Role.DEVELOPER),
			"viewer@demo.local", new CurrentUser(2L, "viewer@demo.local", "데모 열람자", Role.VIEWER),
			"user@demo.local", new CurrentUser(3L, "user@demo.local", "데모 사용자", Role.USER)
	);

	private final HttpSessionSecurityContextRepository contextRepository =
			new HttpSessionSecurityContextRepository();

	@PostMapping("/login")
	DemoLoginResponse login(
			@Valid @RequestBody DemoLoginRequest loginRequest,
			HttpServletRequest request,
			HttpServletResponse response
	) {
		if (loginRequest.email() == null) {
			SecurityContextHolder.clearContext();
			request.getSession().invalidate();
			return new DemoLoginResponse(false, null);
		}

		CurrentUser user = DEMO_USERS.get(loginRequest.email());
		if (user == null) {
			throw new IllegalArgumentException("지원하지 않는 데모 계정입니다.");
		}

		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
				user,
				null,
				user.getAuthorities()
		);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		contextRepository.saveContext(context, request, response);

		return new DemoLoginResponse(true, CurrentUserResponse.from(user));
	}

	public record DemoLoginRequest(String email) {
	}

	public record DemoLoginResponse(boolean authenticated, CurrentUserResponse user) {
	}
}
