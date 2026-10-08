package kr.ac.pusan.feedback.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackDetail;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackPage;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackSummary;
import kr.ac.pusan.feedback.admin.dto.AnswerUpsertRequest;
import kr.ac.pusan.feedback.admin.dto.FeedbackUpdateRequest;
import kr.ac.pusan.feedback.admin.dto.PriorityRequestResponse;
import kr.ac.pusan.feedback.auth.AppUserPrincipal;
import kr.ac.pusan.feedback.auth.CurrentUser;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackSort;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;
import kr.ac.pusan.feedback.notification.NotificationService;

/**
 * 관리용 피드백 API. 기획안 6-3, 6-4, 9장.
 *
 * <p>목록·상세 조회(1단계)와 상태·유형·중요도·담당자 변경(원래 3단계 예정이었으나,
 * 프론트가 접수칸에서도 상세 패널로 바로 바꿀 수 있게 하면서 앞당겨 구현했다)을
 * B 가 실제 구현으로 교체했다(기획안 13-5). 답변 등록과 우선 처리 요청도 DB에 저장한다.
 *
 * <p>권한은 0-1단계 SecurityConfig 가 이미 선언했다. 조회는 DEVELOPER·VIEWER, 변경은 DEVELOPER 전용이며
 * VIEWER 의 변경 호출은 403 이다(기획안 9장) — 여기서 따로 검사하지 않는다.
 */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/feedbacks")
@PreAuthorize("principal.status == T(kr.ac.pusan.feedback.common.enums.UserStatus).ACTIVE")
public class AdminFeedbackController {

	private final FeedbackRepository feedbackRepository;
	private final AnswerRepository answerRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	AdminFeedbackController(
			FeedbackRepository feedbackRepository,
			AnswerRepository answerRepository,
			UserRepository userRepository,
			NotificationService notificationService) {
		this.feedbackRepository = feedbackRepository;
		this.answerRepository = answerRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	@Operation(summary = "관리용 피드백 목록 조회",
			description = "필터는 사이트·상태·유형·기간·회원 여부·답변 유무·우선 처리 요청 여부이고 "
					+ "정렬은 PRIORITY·LATEST·OLDEST 다. "
					+ "어떤 정렬을 골라도 우선 처리 요청 항목이 최상단에 고정된다.")
	@GetMapping
	@Transactional(readOnly = true)
	public AdminFeedbackPage feedbacks(
			@RequestParam(required = false) String project,
			@RequestParam(required = false) FeedbackStatus status,
			@RequestParam(required = false) FeedbackCategory category,
			@RequestParam(required = false) FeedbackSort sort,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) AuthorType authorType,
			@RequestParam(required = false) Boolean answered,
			@RequestParam(required = false) Boolean priorityRequested,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		validatePage(page, size);
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
		}

		// to 는 날짜(하루 단위)라서, 그날 전체를 포함하도록 다음날 0시 "미만"으로 바꾼다.
		LocalDateTime fromInclusive = from == null ? null : from.atStartOfDay();
		LocalDateTime toExclusive = to == null ? null : to.plusDays(1).atStartOfDay();

		// answered(답변 유무)는 자바에서 거르지 않고 쿼리의 EXISTS 서브쿼리가 DB에서 직접 거른다.
		// 조건에 안 맞는 행은 애초에 서버로 전송되지 않으니, 목록이 커져도 느려지지 않는다.
		FeedbackSort effectiveSort = sort == null ? FeedbackSort.LATEST : sort;
		Page<Feedback> matched = feedbackRepository.searchForAdmin(
				project, status, category, authorType, fromInclusive, toExclusive, answered, priorityRequested,
				effectiveSort.name(), PageRequest.of(page, size));

		// answeredIds는 필터링용이 아니라 DTO의 answered 필드를 채우는 표시용이다 — 건마다 따로
		// 조회하면 N+1이 나니 한 번에 구한다.
		Set<Long> answeredIds = answeredFeedbackIds(matched.getContent());

		List<AdminFeedbackSummary> items = matched.getContent().stream()
				.map(feedback -> AdminFeedbackMapper.toSummary(feedback, answeredIds.contains(feedback.getId())))
				.toList();

		// totalCount 는 이번 페이지 건수가 아니라 필터 조건 전체 건수다(AdminFeedbackPage 계약).
		return new AdminFeedbackPage(items, matched.getTotalElements());
	}

	@Operation(summary = "관리용 피드백 상세 조회",
			description = "목록 항목의 모든 필드에 본문, 내부 타임스탬프, 답변을 더해 내려준다.")
	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public AdminFeedbackDetail feedback(@PathVariable Long id) {
		Feedback feedback = feedbackRepository.findDetailById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."));
		Answer answer = answerRepository.findByFeedbackId(id).orElse(null);
		return AdminFeedbackMapper.toDetail(feedback, answer);
	}

	@Operation(summary = "상태·유형·중요도·담당자 변경",
			description = "개발자 전용이다. 보낸 필드만 바꾼다. 작성자가 고른 reportedPriority 는 바꿀 수 없다. "
					+ "assigneeId 는 기획에 없던 기능으로, DEVELOPER·ACTIVE 계정이 아니면 400 이다. "
					+ "담당자를 해제하려면 assigneeId 대신 unassign 을 true 로 보낸다.")
	@PatchMapping("/{id}")
	@Transactional
	public AdminFeedbackDetail update(@PathVariable Long id, @RequestBody FeedbackUpdateRequest request) {
		Feedback feedback = feedbackRepository.findDetailById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."));

		// unassign이 true면 assigneeId는 보든 말든 무시한다 — 조회·검증(400)까지 할 필요가 없다.
		User assignee = request.unassign() ? null : resolveAssignee(request.assigneeId());

		feedback.applyAdminUpdate(request.status(), request.category(), request.priority(), assignee,
				request.unassign(), LocalDateTime.now());

		// save()가 돌려주는 인스턴스를 쓰지 않는다. merge()는 내부적으로 DB에서 다시 읽어온 새 인스턴스를
		// 돌려줄 수 있는데, 그 인스턴스는 project·author·assignee가 초기화 안 된 지연 로딩 프록시라서
		// toDetail()에서 LazyInitializationException이 난다. findDetailById로 이미 JOIN FETCH 해 둔
		// feedback을 그대로 쓰면 이 문제가 없다. (변경 사항은 save() 호출만으로 DB에 반영된다.)
		feedbackRepository.save(feedback);

		Answer answer = answerRepository.findByFeedbackId(id).orElse(null);
		return AdminFeedbackMapper.toDetail(feedback, answer);
	}

	/** assigneeId 가 null 이면 담당자를 바꾸지 않는다(null 반환). 보낸 id가 DEVELOPER·ACTIVE 가 아니면 400. */
	private User resolveAssignee(Long assigneeId) {
		if (assigneeId == null) {
			return null;
		}
		return userRepository.findById(assigneeId)
				.filter(user -> user.getRole() == Role.DEVELOPER && user.getStatus() == UserStatus.ACTIVE)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "담당자로 지정할 수 없는 계정입니다."));
	}

	@Operation(summary = "답변 등록·수정",
			description = "개발자 전용이다. 피드백 1건당 답변 1건이다. markDone 이 true 면 상태를 DONE 으로 함께 바꾼다. "
					+ "비회원 피드백은 답변 대상이 아니라 400 이다.")
	@PutMapping("/{id}/answer")
	@Transactional
	public AnswerResponse upsertAnswer(
			@PathVariable Long id,
			@Valid @RequestBody AnswerUpsertRequest request,
			@CurrentUser AppUserPrincipal currentUser) {
		Feedback feedback = feedbackRepository.findDetailById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."));

		// 비회원(AuthorType.GUEST) 피드백은 답변 대상이 아니다(기획안 6-4).
		if (feedback.getAuthorType() == AuthorType.GUEST) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비회원 피드백은 답변 대상이 아닙니다.");
		}

		// 이 엔드포인트는 DEVELOPER 전용이라(SecurityConfig) currentUser 가 null 일 수 없다.
		User author = userRepository.findByEmail(currentUser.getEmail())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

		LocalDateTime now = LocalDateTime.now();
		Answer answer = answerRepository.findByFeedbackId(id).orElse(null);
		boolean firstAnswer = answer == null;

		if (firstAnswer) {
			answer = Answer.builder()
					.feedback(feedback)
					.author(author)
					.content(request.content())
					.createdAt(now)
					.updatedAt(now)
					.build();
		} else {
			answer.changeContent(request.content(), now);
		}
		answer = answerRepository.save(answer);

		if (firstAnswer) {
			feedback.recordFirstAnswerIfAbsent(now);
		}
		if (request.markDone()) {
			feedback.applyAdminUpdate(FeedbackStatus.DONE, null, null, null, false, now);
		}
		feedbackRepository.save(feedback);

		// 답변을 수정한 경우에는 보내지 않는다(기획안 7장) — 첫 등록일 때만 호출한다.
		if (firstAnswer) {
			notificationService.notifyAnswerRegistered(feedback);
		}

		return new AnswerResponse(answer.getId(), answer.getContent(), answer.getCreatedAt(), answer.getUpdatedAt());
	}

	@Operation(summary = "우선 처리 요청 토글",
			description = "열람자 전용이다. 요청하면 중요도가 HIGH 로 올라가고 목록 최상단에 고정된다. 다시 호출하면 해제된다.")
	@PostMapping("/{id}/priority-request")
	@Transactional
	public PriorityRequestResponse priorityRequest(
			@PathVariable Long id,
			@CurrentUser AppUserPrincipal currentUser) {
		Feedback feedback = feedbackRepository.findDetailById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."));
		User requester = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 계정을 찾을 수 없습니다."));
		feedback.togglePriorityRequest(requester);

		// 요청을 거는 순간에만 보낸다(기획안 7장) — 해제할 때는 보내지 않는다.
		if (feedback.isPriorityRequested()) {
			List<User> developers = userRepository.findAllByRoleAndStatus(Role.DEVELOPER, UserStatus.ACTIVE);
			notificationService.notifyPriorityRequested(feedback, developers);
		}

		return new PriorityRequestResponse(feedback.getId(), feedback.isPriorityRequested(), feedback.getPriority());
	}

	private static void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("page는 0 이상, size는 1~100이어야 합니다.");
		}
	}

	// ------------------------------------------------------------------
	// Entity → DTO 변환
	// ------------------------------------------------------------------

	/**
	 * 주어진 피드백들 중 답변이 달린 id만 한 번의 쿼리로 구한다(목록에서 건마다 조회하는 N+1을 막는다).
	 * AdminDashboardController 에도 같은 모양의 메서드가 있다 — answerRepository 인스턴스가 필요해서
	 * AdminFeedbackMapper(정적 유틸)로는 뺄 수 없었다. 로직 자체가 세 줄이라 중복 비용이 작다고 판단했다.
	 */
	private Set<Long> answeredFeedbackIds(List<Feedback> feedbacks) {
		List<Long> ids = feedbacks.stream().map(Feedback::getId).toList();
		if (ids.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(answerRepository.findFeedbackIdsWithAnswer(ids));
	}
}
