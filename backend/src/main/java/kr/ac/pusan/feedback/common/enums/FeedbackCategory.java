package kr.ac.pusan.feedback.common.enums;

/**
 * 피드백 유형. 기획안 4-1.
 *
 * <p>유형은 작성자가 선택하고, 개발자가 나중에 변경할 수 있다.
 * JSON 으로는 상수 이름(BUG/FEATURE/ETC)이 그대로 내려간다. label 은 화면 표기용 참고값이다.
 */
public enum FeedbackCategory {

	/** 서비스 이용 중 발생한 오류 또는 비정상 동작 */
	BUG("오류 신고"),

	/** 추가를 요청하는 기능 */
	FEATURE("기능 제안"),

	/** 위 두 유형에 해당하지 않는 의견 */
	ETC("기타 의견");

	private final String label;

	FeedbackCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
