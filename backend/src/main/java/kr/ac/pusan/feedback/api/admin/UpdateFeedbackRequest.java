package kr.ac.pusan.feedback.api.admin;

import jakarta.validation.constraints.AssertTrue;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;

public record UpdateFeedbackRequest(
		FeedbackStatus status,
		FeedbackType type,
		Priority priority
) {
	@AssertTrue(message = "status, type, priority 중 하나 이상을 입력해 주세요.")
	public boolean isAnyFieldPresent() {
		return status != null || type != null || priority != null;
	}
}
