package kr.ac.pusan.feedback.common.enums;

/**
 * 중요도. 기획안 4-3.
 *
 * <p>작성자가 고른 값(reportedPriority)과 개발자가 확정한 값(priority) 두 필드에 같은 enum 을 쓴다.
 * 정렬에 사용하는 값은 개발자 확정값뿐이다.
 */
public enum Priority {

	HIGH("높음"),
	NORMAL("보통"),
	LOW("낮음");

	private final String label;

	Priority(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
