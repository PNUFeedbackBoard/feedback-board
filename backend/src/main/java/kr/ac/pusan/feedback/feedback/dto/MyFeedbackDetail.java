package kr.ac.pusan.feedback.feedback.dto;

import java.time.LocalDateTime;

import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 내 문의 상세. GET /api/me/feedbacks/{id}
 *
 * <p>answer 는 답변이 없으면 null 이다. 화면은 null 인 경우 안내 문구를 표시한다(기획안 5-3).
 * 개발자가 확정한 priority 는 사용자에게 내보내지 않고, 작성자가 고른 reportedPriority 만 내보낸다.
 */
public record MyFeedbackDetail(
		Long id,
		String projectCode,
		String projectName,
		String title,
		String content,
		FeedbackCategory category,
		FeedbackStatus status,
		Priority reportedPriority,
		LocalDateTime createdAt,
		AnswerView answer
) {

	/**
	 * 사용자에게 보여 줄 답변. 내부 식별자를 포함하지 않는다.
	 */
	public record AnswerView(
			String content,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {
	}
}
