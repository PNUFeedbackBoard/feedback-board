package kr.ac.pusan.feedback.api.admin;

import java.time.Instant;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;

public record AdminFeedbackResponse(
		Long id,
		ProjectSummary project,
		AuthorSummary author,
		String title,
		String content,
		FeedbackType type,
		FeedbackStatus status,
		Priority reportedPriority,
		Priority priority,
		boolean priorityRequested,
		Instant createdAt,
		Instant updatedAt,
		AnswerSummary answer
) {
	public record ProjectSummary(String code, String name) {
	}

	public record AuthorSummary(Long id, String email, String displayName, boolean guest) {
	}

	public record AnswerSummary(String content, String developerEmail, Instant updatedAt) {
	}
}
