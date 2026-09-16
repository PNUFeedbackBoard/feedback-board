package kr.ac.pusan.feedback.common.enums;

/**
 * 피드백 작성자 구분. 기획안 8장.
 *
 * <p>GUEST 인 피드백은 author 가 null 이며 답변 대상이 아니다.
 */
public enum AuthorType {

	/** 로그인한 회원 */
	MEMBER,

	/** 비회원 */
	GUEST
}
