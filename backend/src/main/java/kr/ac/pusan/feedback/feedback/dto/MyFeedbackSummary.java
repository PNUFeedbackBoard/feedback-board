package kr.ac.pusan.feedback.feedback.dto;

import java.time.LocalDateTime;

import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;

/**
 * 내 문의 목록 항목. GET /api/me/feedbacks
 *
 * <p>사용자용이므로 중요도·우선 처리 요청·내부 타임스탬프는 포함하지 않는다(기획안 13-4).
 */
public record MyFeedbackSummary(
		Long id,
		String projectCode,
		String projectName,
		String title,
		FeedbackCategory category,
		FeedbackStatus status,
		LocalDateTime createdAt,
		boolean answered
) {
}
