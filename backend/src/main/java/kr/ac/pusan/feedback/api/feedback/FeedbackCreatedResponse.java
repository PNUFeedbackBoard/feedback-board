package kr.ac.pusan.feedback.api.feedback;

import java.time.Instant;
import kr.ac.pusan.feedback.domain.FeedbackStatus;

public record FeedbackCreatedResponse(
		Long id,
		FeedbackStatus status,
		Instant createdAt
) {
}
