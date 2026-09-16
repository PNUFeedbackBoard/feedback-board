package kr.ac.pusan.feedback.api.admin;

import jakarta.validation.constraints.AssertTrue;
import kr.ac.pusan.feedback.domain.Role;

public record UpdateAccountRequest(
		Role role,
		Boolean approved
) {
	@AssertTrue(message = "role, approved 중 하나 이상을 입력해 주세요.")
	public boolean isAnyFieldPresent() {
		return role != null || approved != null;
	}
}
