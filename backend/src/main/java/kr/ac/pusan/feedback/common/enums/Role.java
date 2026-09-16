package kr.ac.pusan.feedback.common.enums;

/**
 * 계정 역할. 기획안 3-1.
 *
 * <p>관리용 사이트에 접근할 수 있는 역할은 DEVELOPER 와 VIEWER 두 가지다.
 * 비회원은 계정을 만들지 않으므로 여기에 해당 상수를 두지 않는다.
 */
public enum Role {

	/** 피드백 등록, 본인 피드백과 답변 조회 */
	USER,

	/** 전체 조회 및 수정, 상태 변경, 답변 작성, 계정 승인 */
	DEVELOPER,

	/** 전체 조회, 대시보드 조회, 우선 처리 요청 */
	VIEWER
}
