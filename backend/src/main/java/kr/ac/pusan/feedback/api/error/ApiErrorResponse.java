package kr.ac.pusan.feedback.api.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String code,
		String message,
		String path,
		List<FieldErrorDetail> details
) {

	public record FieldErrorDetail(String field, String message) {
	}
}
