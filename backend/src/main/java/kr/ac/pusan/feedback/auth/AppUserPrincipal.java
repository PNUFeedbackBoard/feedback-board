package kr.ac.pusan.feedback.auth;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.User;
import lombok.Getter;

/**
 * 로그인한 계정을 담는 인증 주체(principal).
 *
 * <p>스프링 시큐리티의 SecurityContext 에 들어가는 객체이며, 세션에 저장된다.
 * 엔티티(User)를 그대로 넣지 않는 이유는 두 가지다.
 * <ul>
 *   <li>엔티티를 세션에 넣으면 영속성 컨텍스트가 끊긴 상태로 떠돌아 LAZY 로딩에서 터진다.</li>
 *   <li>인증에 필요한 값만 복사해 두면 7단계에서 구글 로그인으로 바꿔도 이 클래스만 채워주면 된다.</li>
 * </ul>
 *
 * <p>권한(authority)은 계약대로 "ROLE_" + role.name() 하나만 내려준다.
 * 그래서 SecurityConfig 에서 hasRole("DEVELOPER") 같은 표기를 그대로 쓸 수 있다.
 */
@Getter
public class AppUserPrincipal implements UserDetails {

	@Serial
	private static final long serialVersionUID = 1L;

	/** 스프링 시큐리티의 hasRole(...) 이 기대하는 권한 접두사 */
	private static final String ROLE_PREFIX = "ROLE_";

	private final Long id;
	private final String email;
	private final String name;
	private final Role role;
	private final UserStatus status;

	public AppUserPrincipal(Long id, String email, String name, Role role, UserStatus status) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.role = role;
		this.status = status;
	}

	/** 계정 엔티티에서 인증 주체를 만든다. 7단계 구글 로그인도 마지막에는 이 메서드를 부르면 된다. */
	public static AppUserPrincipal from(User user) {
		return new AppUserPrincipal(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getRole(),
				user.getStatus()
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + this.role.name()));
	}

	/** 자체 비밀번호를 발급하지 않는다(기획안 10장). 항상 null 이다. */
	@Override
	public String getPassword() {
		return null;
	}

	/** 계정 식별자는 이메일이다. 구글 로그인으로 바뀌어도 동일하다. */
	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * PENDING 계정도 로그인 자체는 된다. 승인 대기 화면을 보여줘야 하기 때문이다(기획안 3-2).
	 * 막아야 하는 것은 DISABLED 뿐이다.
	 */
	@Override
	public boolean isEnabled() {
		return this.status != UserStatus.DISABLED;
	}

	// TODO(B, 7단계): 관리용 화면은 status 가 ACTIVE 인 계정만 허용해야 한다.
	// 지금은 경로 권한(SecurityConfig)만으로 막고, 상태 검사는 구글 로그인 전환과 함께 넣는다.
}
