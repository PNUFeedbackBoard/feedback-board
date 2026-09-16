package kr.ac.pusan.feedback.api.feedback;

import kr.ac.pusan.feedback.domain.Priority;

public record PriorityRequestResponse(
		Long feedbackId,
		boolean priorityRequested,
		Priority priority
) {
}
