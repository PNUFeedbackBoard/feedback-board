package kr.ac.pusan.feedback.api.admin;

import java.util.List;

public record AdminFeedbackPageResponse(
		List<AdminFeedbackResponse> items,
		int page,
		int size,
		long totalElements
) {
}
