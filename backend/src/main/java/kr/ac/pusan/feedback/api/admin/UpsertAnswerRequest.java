package kr.ac.pusan.feedback.api.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertAnswerRequest(
		@NotBlank
		@Size(max = 5000)
		String content
) {
}
