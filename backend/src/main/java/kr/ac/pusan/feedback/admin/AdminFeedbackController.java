package kr.ac.pusan.feedback.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackDetail;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackPage;
import kr.ac.pusan.feedback.admin.dto.AnswerUpsertRequest;
import kr.ac.pusan.feedback.admin.dto.FeedbackUpdateRequest;
import kr.ac.pusan.feedback.admin.dto.PriorityRequestResponse;
import kr.ac.pusan.feedback.common.StubData;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackSort;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;

/**
 * 관리용 피드백 API. 기획안 6-3, 6-4, 9장.
 *
 * <p>0-1단계 스텁이다. 모든 응답은 StubData 의 고정값이며 필터·정렬·권한 검사를 하지 않는다.
 * 목록 14건에 상태 4종·유형 3종·중요도 3종과 우선 처리 요청 2건, 비회원 3건이 들어 있어
 * 칸반 3열, 반영 불가 목록, 비회원 비활성 표시를 스텁만으로 확인할 수 있다.
 *
 * <p>권한은 1단계에서 붙인다. 조회는 DEVELOPER·VIEWER, 변경은 DEVELOPER 전용이며
 * VIEWER 의 변경 호출은 403 이다(기획안 9장).
 */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/feedbacks")
public class AdminFeedbackController {

	// TODO(B, 1단계): 실제 필터·정렬·페이지 조회로 교체한다.
	//                 정렬 1순위는 항상 우선 처리 요청 여부이고, sort 값은 2순위로 적용한다(기획안 4-5).
	//                 totalCount 는 페이지가 아니라 필터 조건 전체의 건수다.
	@Operation(summary = "관리용 피드백 목록 조회",
			description = "필터는 사이트·상태·유형·기간·회원 여부이고 정렬은 PRIORITY·LATEST·OLDEST 다. "
					+ "어떤 정렬을 골라도 우선 처리 요청 항목이 최상단에 고정된다.")
	@GetMapping
	public AdminFeedbackPage feedbacks(
			@RequestParam(required = false) String project,
			@RequestParam(required = false) FeedbackStatus status,
			@RequestParam(required = false) FeedbackCategory category,
			@RequestParam(required = false) FeedbackSort sort,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) AuthorType authorType,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return StubData.adminFeedbackPage();
	}

	// TODO(B, 1단계): 실제 조회로 교체한다. 없는 id 면 404 를 반환한다.
	@Operation(summary = "관리용 피드백 상세 조회",
			description = "목록 항목의 모든 필드에 본문, 내부 타임스탬프, 답변을 더해 내려준다.")
	@GetMapping("/{id}")
	public AdminFeedbackDetail feedback(@PathVariable Long id) {
		return StubData.adminFeedbackDetail(id);
	}

	// TODO(B, 3단계): 실제 변경으로 교체한다.
	//                 status·category·priority 중 null 이 아닌 값만 바꾼다.
	//                 DONE·REJECTED 로 바뀌면 closedAt 을 기록하고, VIEWER 가 호출하면 403 을 반환한다.
	@Operation(summary = "상태·유형·중요도 변경",
			description = "개발자 전용이다. 보낸 필드만 바꾼다. 작성자가 고른 reportedPriority 는 바꿀 수 없다.")
	@PatchMapping("/{id}")
	public AdminFeedbackDetail update(@PathVariable Long id, @RequestBody FeedbackUpdateRequest request) {
		return StubData.adminFeedbackDetail(id);
	}

	// TODO(A, 3단계): 실제 답변 등록·수정으로 교체한다.
	//                 피드백 1건당 답변 1건이므로 있으면 수정하고 없으면 만든다.
	//                 첫 등록이면 firstAnsweredAt 을 기록하고 작성자에게 알림을 보낸다. 수정 시에는 보내지 않는다(기획안 7장).
	//                 비회원(AuthorType.GUEST) 피드백은 답변 대상이 아니므로 거부한다.
	@Operation(summary = "답변 등록·수정",
			description = "개발자 전용이다. 피드백 1건당 답변 1건이다. markDone 이 true 면 상태를 DONE 으로 함께 바꾼다.")
	@PutMapping("/{id}/answer")
	public AnswerResponse upsertAnswer(@PathVariable Long id, @RequestBody AnswerUpsertRequest request) {
		return StubData.answer(id);
	}

	// TODO(A, 5단계): 실제 토글로 교체한다.
	//                 요청 시 priorityRequested 를 true 로, priority 를 HIGH 로 바꾸고 개발자 전원에게 메일을 보낸다.
	//                 해제는 요청한 열람자 본인만 할 수 있다(기획안 4-4).
	@Operation(summary = "우선 처리 요청 토글",
			description = "열람자 전용이다. 요청하면 중요도가 HIGH 로 올라가고 목록 최상단에 고정된다. 다시 호출하면 해제된다.")
	@PostMapping("/{id}/priority-request")
	public PriorityRequestResponse priorityRequest(@PathVariable Long id) {
		return StubData.priorityRequest(id);
	}
}
