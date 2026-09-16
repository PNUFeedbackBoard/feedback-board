package kr.ac.pusan.feedback.common.enums;

/**
 * 피드백 진행 상태. 기획안 4-2.
 *
 * <p>칸반 보드는 RECEIVED / IN_PROGRESS / DONE 세 가지를 열로 사용하고,
 * REJECTED 는 보드 밖의 별도 목록으로 표시한다.
 */
public enum FeedbackStatus {

	/** 등록 직후 부여되는 초기 상태 (칸반 1열) */
	RECEIVED("접수"),

	/** 수정 작업에 착수한 상태 (칸반 2열) */
	IN_PROGRESS("처리 중"),

	/** 수정이 반영되어 추가 조치가 필요 없는 상태 (칸반 3열) */
	DONE("처리 완료"),

	/** 중복·오신고이거나 현재 반영이 어렵다고 판단한 상태 (보드 밖) */
	REJECTED("반영 불가");

	private final String label;

	FeedbackStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
