package kr.ac.pusan.feedback.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.pusan.feedback.api.error.ApiErrorResponse;

/**
 * 보안 경로 패턴. 기획안 9장(접근 권한), 13-4(공유 파일), 13-5(0-1단계 산출물).
 *
 * <p><b>이 파일은 0-1단계에서 계약의 모든 경로를 선언한다. 이후 담당자가 다시 열 일이 없어야 한다.</b>
 * 새 API 를 만들 때 여기에 경로를 추가해야 한다면 계약에 없던 경로라는 뜻이므로 팀에 먼저 공유한다.
 *
 * <p>인증 방식은 세션이다. 지금은 데모 로그인(POST /api/dev/login)이,
 * 7단계부터는 구글 로그인이 같은 세션을 만든다. 경로 규칙은 그대로 둔다.
 *
 * <p>권한 표기 규칙: hasRole("DEVELOPER") 는 권한 문자열 "ROLE_DEVELOPER" 를 뜻한다.
 * 그 문자열은 {@code AppUserPrincipal#getAuthorities()} 가 "ROLE_" + Role 이름으로 만들어 준다.
 */
@Configuration
@EnableWebSecurity
// 경로 규칙만으로 부족한 검사(예: 본인 피드백인지)는 @PreAuthorize 로 붙일 수 있도록 미리 켜 둔다.
@EnableMethodSecurity
public class SecurityConfig {

	private final ObjectMapper objectMapper;

	SecurityConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// TODO(B, 7단계): 구글 로그인 전환 시 CSRF 재검토.
				// 지금은 프론트가 별도 개발 서버(5173)에서 돌고 데모 로그인만 쓰므로 끈다.
				.csrf(csrf -> csrf.disable())
				// h2-console 이 프레임을 쓴다. dev 프로필에서만 켜지는 경로다.
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				// 로그인 폼과 basic 인증을 쓰지 않는다. 켜 두면 401 대신 로그인 폼으로 리다이렉트되어 프론트가 깨진다.
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(unauthorizedEntryPoint())
						.accessDeniedHandler(forbiddenHandler()))
				.authorizeHttpRequests(authorize -> authorize

						// --- 공개: 로그인 없이 호출한다 -------------------------------------------
						// 오류 전달(forward /error)까지 막히면 원래 오류 대신 403 이 나간다.
						.requestMatchers("/error").permitAll()
						.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/h2-console/**"
						).permitAll()
						// 데모 로그인. 운영 프로필에서는 컨트롤러가 등록되지 않아 404 다.
						.requestMatchers("/api/dev/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/projects").permitAll()
						// 비회원 등록을 허용한다(기획안 9장).
						.requestMatchers(HttpMethod.POST, "/api/feedbacks").permitAll()

						// --- 회원: 로그인만 되어 있으면 된다 ---------------------------------------
						.requestMatchers("/api/me", "/api/me/**").authenticated()

						// --- 관리: 좁은 규칙을 먼저 둔다. 순서가 바뀌면 권한이 헐거워진다 -----------
						// 우선 처리 요청은 열람자 전용이다(기획안 4-4).
						.requestMatchers(HttpMethod.POST, "/api/admin/feedbacks/*/priority-request")
						.hasRole("VIEWER")
						// 계정 관리는 조회까지 개발자 전용이다(기획안 6-5).
						.requestMatchers("/api/admin/users", "/api/admin/users/**").hasRole("DEVELOPER")
						// 그 밖의 관리용 조회는 개발자와 열람자가 함께 본다.
						.requestMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("DEVELOPER", "VIEWER")
						// 조회가 아닌 관리용 요청(PATCH·PUT·POST·DELETE)은 개발자만 한다.
						// 열람자가 호출하면 여기서 403 이 된다(기획안 9장 마지막 줄).
						.requestMatchers("/api/admin/**").hasRole("DEVELOPER")

						// --- 위에 없는 /api 경로는 막는다. 계약에 없는 주소가 공개되지 않게 한다 ----
						.requestMatchers("/api/**").authenticated()

						// --- API 가 아닌 요청(정적 파일, 프론트 화면)은 공개한다 -------------------
						.anyRequest().permitAll());

		return http.build();
	}

	/** 로그인하지 않은 요청: 401 을 JSON 으로 준다. */
	private AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) ->
				writeError(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
	}

	/** 로그인은 했지만 역할이 모자란 요청: 403 을 JSON 으로 준다. */
	private AccessDeniedHandler forbiddenHandler() {
		return (request, response, accessDeniedException) ->
				writeError(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다.");
	}

	/**
	 * 오류 본문을 GlobalExceptionHandler 와 같은 ApiErrorResponse 형식으로 맞춘다.
	 * 프론트가 오류 처리 코드를 한 벌만 쓰게 하기 위한 것이다.
	 */
	private void writeError(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpStatus status,
			String code,
			String message
	) throws IOException {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				code,
				message,
				request.getRequestURI(),
				List.of()
		);

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), body);
	}
}
