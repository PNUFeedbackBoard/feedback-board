package kr.ac.pusan.feedback.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 피드백")
@RestController
@RequestMapping("/api/admin/feedbacks")
public class AdminFeedbackController {

	private static final Instant CREATED_AT = Instant.parse("2026-09-01T09:00:00Z");

	@Operation(summary = "관리자 피드백 목록 조회", description = "우선 처리 요청 항목은 모든 정렬에서 최상단에 고정합니다.")
	@GetMapping
	AdminFeedbackPageResponse findAll(
			@RequestParam(required = false) String projectCode,
			@RequestParam(required = false) FeedbackStatus status,
			@RequestParam(required = false) FeedbackType type,
			@RequestParam(required = false) Priority priority,
			@RequestParam(defaultValue = "LATEST") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		return new AdminFeedbackPageResponse(List.of(sampleFeedback(1L)), page, size, 40);
	}

	@Operation(summary = "관리자 피드백 상세 조회")
	@GetMapping("/{feedbackId}")
	AdminFeedbackResponse findOne(@PathVariable Long feedbackId) {
		return sampleFeedback(feedbackId);
	}

	@Operation(summary = "피드백 상태·유형·중요도 변경")
	@PatchMapping("/{feedbackId}")
	AdminFeedbackResponse update(
			@PathVariable Long feedbackId,
			@Valid @RequestBody UpdateFeedbackRequest request
	) {
		AdminFeedbackResponse original = sampleFeedback(feedbackId);
		return new AdminFeedbackResponse(
				original.id(),
				original.project(),
				original.author(),
				original.title(),
				original.content(),
				request.type() == null ? original.type() : request.type(),
				request.status() == null ? original.status() : request.status(),
				original.reportedPriority(),
				request.priority() == null ? original.priority() : request.priority(),
				original.priorityRequested(),
				original.createdAt(),
				Instant.now(),
				original.answer()
		);
	}

	@Operation(summary = "개발자 답변 등록·수정")
	@PutMapping("/{feedbackId}/answer")
	AdminFeedbackResponse.AnswerSummary upsertAnswer(
			@PathVariable Long feedbackId,
			@Valid @RequestBody UpsertAnswerRequest request
	) {
		return new AdminFeedbackResponse.AnswerSummary(request.content(), "dev@demo.local", Instant.now());
	}

	private AdminFeedbackResponse sampleFeedback(Long id) {
		return new AdminFeedbackResponse(
				id,
				new AdminFeedbackResponse.ProjectSummary("aipms", "AIPMS"),
				new AdminFeedbackResponse.AuthorSummary(3L, "user@demo.local", "데모 사용자", false),
				"검색 결과가 보이지 않아요",
				"검색 조건을 선택한 뒤 결과 영역이 비어 있습니다.",
				FeedbackType.BUG_REPORT,
				FeedbackStatus.IN_PROGRESS,
				Priority.HIGH,
				Priority.HIGH,
				true,
				CREATED_AT,
				CREATED_AT.plusSeconds(3600),
				new AdminFeedbackResponse.AnswerSummary(
						"현재 원인을 확인하고 있습니다.",
						"dev@demo.local",
						CREATED_AT.plusSeconds(3600)
				)
		);
	}
}
