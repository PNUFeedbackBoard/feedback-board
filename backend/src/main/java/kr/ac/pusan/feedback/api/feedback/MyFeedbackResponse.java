package kr.ac.pusan.feedback.api.feedback;

import java.time.Instant;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;

public record MyFeedbackResponse(
		Long id,
		String projectCode,
		String title,
		String content,
		FeedbackType type,
		FeedbackStatus status,
		Instant createdAt,
		AnswerSummary answer
) {
	public record AnswerSummary(String content, Instant answeredAt) {
	}
}
