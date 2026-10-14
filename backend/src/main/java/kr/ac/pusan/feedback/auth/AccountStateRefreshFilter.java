package kr.ac.pusan.feedback.auth;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.repository.UserRepository;

/**
 * 세션에 복사된 역할·상태를 각 API 요청에서 DB의 최신 계정 정보로 갱신한다.
 *
 * <p>관리자가 역할을 바꿔도 기존 세션의 principal은 자동으로 바뀌지 않는다. 이 필터가
 * 인가 필터보다 먼저 실행되어 강등·승격을 다음 요청부터 반영하고, 삭제되거나 비활성화된
 * 계정의 세션은 즉시 폐기한다. 정적 파일 요청에는 DB 조회가 필요하지 않아 /api 경로만 다룬다.
 */
public class AccountStateRefreshFilter extends OncePerRequestFilter {

	private final UserRepository userRepository;

	public AccountStateRefreshFilter(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
			filterChain.doFilter(request, response);
			return;
		}

		var user = userRepository.findById(principal.getId()).orElse(null);
		if (user == null || user.getStatus() == UserStatus.DISABLED) {
			SecurityContextHolder.clearContext();
			var session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			filterChain.doFilter(request, response);
			return;
		}

		AppUserPrincipal refreshed = AppUserPrincipal.from(user);
		var refreshedAuthentication = UsernamePasswordAuthenticationToken.authenticated(
				refreshed,
				authentication.getCredentials(),
				refreshed.getAuthorities());
		refreshedAuthentication.setDetails(authentication.getDetails());
		SecurityContextHolder.getContext().setAuthentication(refreshedAuthentication);

		filterChain.doFilter(request, response);
	}
}
