package kr.ac.pusan.feedback.api.feedback;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 피드백")
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

	private static final Instant CREATED_AT = Instant.parse("2026-09-01T09:00:00Z");

	@Operation(summary = "피드백 등록")
	@PostMapping
	FeedbackCreatedResponse create(@Valid @RequestBody CreateFeedbackRequest request) {
		return new FeedbackCreatedResponse(1001L, FeedbackStatus.RECEIVED, Instant.now());
	}

	@Operation(summary = "내 문의 목록 조회")
	@GetMapping("/my")
	List<MyFeedbackResponse> findMine() {
		return List.of(sampleFeedback(1L));
	}

	@Operation(summary = "내 문의 상세 조회")
	@GetMapping("/my/{feedbackId}")
	MyFeedbackResponse findMyFeedback(@PathVariable Long feedbackId) {
		return sampleFeedback(feedbackId);
	}

	@Operation(summary = "우선 처리 요청")
	@PostMapping("/{feedbackId}/priority-request")
	PriorityRequestResponse requestPriority(@PathVariable Long feedbackId) {
		return new PriorityRequestResponse(feedbackId, true, Priority.HIGH);
	}

	private MyFeedbackResponse sampleFeedback(Long id) {
		return new MyFeedbackResponse(
				id,
				"aipms",
				"검색 결과가 보이지 않아요",
				"검색 조건을 선택한 뒤 결과 영역이 비어 있습니다.",
				FeedbackType.BUG_REPORT,
				FeedbackStatus.IN_PROGRESS,
				CREATED_AT,
				new MyFeedbackResponse.AnswerSummary("현재 원인을 확인하고 있습니다.", CREATED_AT.plusSeconds(3600))
		);
	}
}
