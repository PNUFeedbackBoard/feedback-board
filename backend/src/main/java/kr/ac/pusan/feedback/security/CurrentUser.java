package kr.ac.pusan.feedback.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import kr.ac.pusan.feedback.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CurrentUser(
		Long id,
		String email,
		String displayName,
		Role role
) implements UserDetails, Serializable {

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return email;
	}
}
