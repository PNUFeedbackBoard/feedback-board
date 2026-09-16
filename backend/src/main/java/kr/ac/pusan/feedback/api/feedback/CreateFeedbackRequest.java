package kr.ac.pusan.feedback.api.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;

public record CreateFeedbackRequest(
		@NotBlank
		@Pattern(regexp = "codeplace|aipms|aicms|aicap|srvadm")
		String projectCode,

		@NotBlank
		@Size(max = 200)
		String title,

		@NotBlank
		@Size(max = 5000)
		String content,

		@NotNull
		FeedbackType type,

		@NotNull
		Priority reportedPriority
) {
}
