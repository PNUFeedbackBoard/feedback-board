package kr.ac.pusan.feedback.api.auth;

import kr.ac.pusan.feedback.domain.Role;
import kr.ac.pusan.feedback.security.CurrentUser;

public record CurrentUserResponse(
		Long id,
		String email,
		String displayName,
		Role role
) {
	public static CurrentUserResponse from(CurrentUser user) {
		return new CurrentUserResponse(user.id(), user.email(), user.displayName(), user.role());
	}
}
