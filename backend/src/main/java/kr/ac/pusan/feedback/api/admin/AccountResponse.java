package kr.ac.pusan.feedback.api.admin;

import java.time.Instant;
import kr.ac.pusan.feedback.domain.Role;

public record AccountResponse(
		Long id,
		String email,
		String displayName,
		Role role,
		boolean approved,
		Instant createdAt
) {
}
