package kr.ac.pusan.feedback.admin.dto;

import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;

/**
 * 계정 승인·역할 변경 요청. PATCH /api/admin/users/{id}
 *
 * <p>두 필드 모두 nullable 이며 보낸 값만 바꾼다.
 * 승인은 status 를 ACTIVE 로, 거절·비활성화는 DISABLED 로 보내는 방식이다(기획안 3-2).
 */
public record AdminUserUpdateRequest(
		Role role,
		UserStatus status
) {
}
