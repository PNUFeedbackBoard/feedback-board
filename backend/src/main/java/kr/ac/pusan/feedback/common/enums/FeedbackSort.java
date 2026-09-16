package kr.ac.pusan.feedback.common.enums;

/**
 * 관리용 목록의 정렬 기준. 기획안 4-5.
 *
 * <p>어떤 기준을 고르더라도 우선 처리 요청(priorityRequested) 여부가 항상 1순위로 적용되고,
 * 여기의 값은 2순위로 적용된다.
 */
public enum FeedbackSort {

	/** 우선 처리 요청 → HIGH → NORMAL → LOW → 동일 시 최신순 */
	PRIORITY,

	/** 우선 처리 요청 → 등록일 내림차순 */
	LATEST,

	/** 우선 처리 요청 → 등록일 오름차순 */
	OLDEST
}
