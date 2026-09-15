package kr.ac.pusan.feedback.admin.dto;

import java.time.LocalDateTime;

import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;

/**
 * 계정 항목. GET /api/admin/users, PATCH /api/admin/users/{id}
 *
 * <p>createdAt 은 계정 생성일이며 승인 대기 목록의 가입일로 표시한다(기획안 6-5).
 */
public record AdminUserResponse(
		Long id,
		String email,
		String name,
		Role role,
		UserStatus status,
		LocalDateTime createdAt
) {
}
