package kr.ac.pusan.feedback.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** 우선 처리 요청을 토글할 때 화면이 마지막으로 읽은 피드백 버전. */
public record PriorityRequestRequest(
		@NotNull(message = "피드백 버전이 필요합니다.")
		@PositiveOrZero
		Long version
) {
}
