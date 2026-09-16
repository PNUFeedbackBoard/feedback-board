package kr.ac.pusan.feedback.feedback.dto;

import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;

/**
 * 현재 로그인한 계정 정보. GET /api/me
 *
 * <p>화면은 역할을 스스로 판단하지 않고 여기의 role 값만 사용한다(기획안 13-2).
 * 이 규칙을 지켜야 7단계에서 구글 로그인으로 바꿔도 화면 코드를 고치지 않는다.
 */
public record MeResponse(
		Long id,
		String email,
		String name,
		Role role,
		UserStatus status
) {
}
