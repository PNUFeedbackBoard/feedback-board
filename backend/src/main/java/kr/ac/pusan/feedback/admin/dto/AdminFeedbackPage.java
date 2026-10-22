package kr.ac.pusan.feedback.admin.dto;

import java.util.List;

/**
 * 관리용 목록 응답. GET /api/admin/feedbacks
 *
 * <p>totalCount 는 페이지가 아니라 필터 조건 전체의 건수다. 화면의 페이지 수 계산에 쓴다.
 * hiddenCount 는 현재 프로젝트에서 숨긴 전체 피드백 수이며 개발자에게만 실제 값을 제공한다.
 */
public record AdminFeedbackPage(
		List<AdminFeedbackSummary> items,
		long totalCount,
		long hiddenCount
) {
}
