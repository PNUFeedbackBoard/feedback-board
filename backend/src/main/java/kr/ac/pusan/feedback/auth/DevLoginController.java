package kr.ac.pusan.feedback.auth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.ac.pusan.feedback.api.error.ApiErrorResponse;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import kr.ac.pusan.feedback.feedback.dto.MeResponse;

/**
 * 개발 기간용 데모 로그인. 기획안 10장, 13-2.
 *
 * <p>구글 로그인은 7단계에 붙인다. 그 전까지 역할별 화면을 확인하려면 계정 전환 수단이 필요하다.
 * 이 컨트롤러는 <b>{@code @Profile("dev")} 라서 dev 프로필에서만 빈으로 등록된다.</b>
 * 운영 프로필에서는 클래스 자체가 등록되지 않으므로 /api/dev/login 은 404 다.
 *
 * <p>여기서 만든 세션은 실제 로그인과 완전히 같다. 이후 요청은 JSESSIONID 쿠키로 인증되고,
 * 컨트롤러는 {@link CurrentUser} 로만 사용자를 꺼낸다.
 * 7단계에서 이 클래스를 지우고 구글 로그인 성공 처리에서 같은 방식으로 SecurityContext 를 채우면
 * 화면 코드와 다른 컨트롤러는 손대지 않아도 된다.
 */
@Profile("dev")
@Hidden
@RestController
@RequestMapping("/api/dev")
@Tag(name = "dev", description = "개발 전용 데모 로그인 (dev 프로필에서만 등록된다)")
public class DevLoginController {

	/** 세션(HttpSession)에 인증 정보를 저장하는 표준 구현체 */
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	private final UserRepository userRepository;

	DevLoginController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * 데모 계정으로 세션을 발급한다.
	 *
	 * <p>응답은 GET /api/me 와 같은 MeResponse 형태다. 화면은 로그인 직후 응답만으로 역할을 알 수 있다.
	 */
	@Operation(summary = "데모 로그인", description = "account 에 dev | viewer | user | pending 중 하나를 보낸다.")
	@PostMapping("/login")
	public ResponseEntity<?> login(
			@RequestBody DevLoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		DemoAccount demoAccount = DemoAccount.of(request.account());
		if (demoAccount == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(badRequest(httpRequest, "알 수 없는 데모 계정입니다. dev, viewer, user, pending 중 하나를 보내세요."));
		}

		User user = userRepository.findByEmail(demoAccount.email)
				.orElseGet(() -> userRepository.save(demoAccount.toNewUser()));
		AppUserPrincipal principal = AppUserPrincipal.from(user);
		authenticate(principal, httpRequest, httpResponse);

		return ResponseEntity.ok(new MeResponse(
				principal.getId(),
				principal.getEmail(),
				principal.getName(),
				principal.getRole(),
				principal.getStatus()
		));
	}

	/** 세션을 없앤다. 로그인 상태가 아니어도 204 를 준다. */
	@Operation(summary = "데모 로그아웃", description = "세션을 무효화한다.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
		invalidateSession(httpRequest);
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}

	/**
	 * 인증 정보를 SecurityContext 에 넣고 세션에 저장한다.
	 *
	 * <p>계정을 전환할 때 이전 세션이 남아 있으면 헷갈리므로 먼저 버린다.
	 * 새 세션은 securityContextRepository 가 만든다.
	 */
	private void authenticate(AppUserPrincipal principal, HttpServletRequest request, HttpServletResponse response) {
		invalidateSession(request);

		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
				principal, null, principal.getAuthorities());
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
	}

	private void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	/** 오류 응답 형식은 GlobalExceptionHandler 가 쓰는 것과 같은 ApiErrorResponse 로 맞춘다. */
	private ApiErrorResponse badRequest(HttpServletRequest request, String message) {
		return new ApiErrorResponse(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"UNKNOWN_DEMO_ACCOUNT",
				message,
				request.getRequestURI(),
				List.of()
		);
	}

	/**
	 * 기획안 13-2 의 데모 계정 4종.
	 *
	 * <p>계정 자체는 시드 데이터(DataSeeder)가 넣는다.
	 * 여기 정의는 시드가 아직 돌지 않은 상태에서도 로그인이 되게 하기 위한 대비책이다.
	 */
	private enum DemoAccount {

		DEV("dev", "dev@demo.local", "데모 개발자", Role.DEVELOPER, UserStatus.ACTIVE),
		VIEWER("viewer", "viewer@demo.local", "데모 열람자", Role.VIEWER, UserStatus.ACTIVE),
		USER("user", "user@demo.local", "데모 사용자", Role.USER, UserStatus.ACTIVE),
		PENDING("pending", "pending@demo.local", "승인 대기", Role.USER, UserStatus.PENDING);

		private final String account;
		private final String email;
		private final String name;
		private final Role role;
		private final UserStatus status;

		DemoAccount(String account, String email, String name, Role role, UserStatus status) {
			this.account = account;
			this.email = email;
			this.name = name;
			this.role = role;
			this.status = status;
		}

		/** 없는 계정이면 null 을 준다. 호출부에서 400 으로 바꾼다. */
		static DemoAccount of(String account) {
			if (account == null) {
				return null;
			}
			String key = account.trim().toLowerCase(Locale.ROOT);
			for (DemoAccount candidate : values()) {
				if (candidate.account.equals(key)) {
					return candidate;
				}
			}
			return null;
		}

		User toNewUser() {
			return User.builder()
					.email(this.email)
					.name(this.name)
					.role(this.role)
					.status(this.status)
					.createdAt(LocalDateTime.now())
					.build();
		}
	}
}
