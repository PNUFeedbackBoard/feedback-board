package kr.ac.pusan.feedback.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;

/**
 * 계정 승인·역할 변경 요청. PATCH /api/admin/users/{id}
 *
 * <p>version은 마지막으로 조회한 계정 버전이며 필수다. 나머지 두 필드는 nullable 이고 보낸 값만 바꾼다.
 * 승인은 status 를 ACTIVE 로, 거절·비활성화는 DISABLED 로 보내는 방식이다(기획안 3-2).
 */
public record AdminUserUpdateRequest(
		@NotNull(message = "계정 버전이 필요합니다.")
		@PositiveOrZero
		Long version,
		Role role,
		UserStatus status
) {
}
