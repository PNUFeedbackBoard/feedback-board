package kr.ac.pusan.feedback.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;

/** 여러 피드백의 상태와 담당자를 한 트랜잭션에서 함께 변경한다. */
public record FeedbackBulkUpdateRequest(
		@NotEmpty(message = "피드백을 한 건 이상 선택해 주세요.")
		@Size(max = 100, message = "한 번에 최대 100건까지 변경할 수 있습니다.")
		List<@NotNull @Positive Long> ids,
		FeedbackStatus status,
		Long assigneeId,
		boolean unassign
) {
}
