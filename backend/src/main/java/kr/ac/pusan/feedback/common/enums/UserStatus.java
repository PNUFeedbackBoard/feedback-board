package kr.ac.pusan.feedback.common.enums;

/**
 * 계정 상태. 기획안 3-2.
 *
 * <p>관리용 사이트는 ACTIVE 인 계정만 사용할 수 있다.
 */
public enum UserStatus {

	/** 계정은 생성되었으나 아직 승인되지 않음 */
	PENDING,

	/** 승인 완료 */
	ACTIVE,

	/** 비활성화 */
	DISABLED
}
