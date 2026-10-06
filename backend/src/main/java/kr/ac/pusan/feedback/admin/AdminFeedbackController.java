package kr.ac.pusan.feedback.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
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
import kr.ac.pusan.feedback.common.enums.Priority;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import kr.ac.pusan.feedback.feedback.FeedbackMapper;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;
import lombok.RequiredArgsConstructor;

/** 관리 화면에서 사용하는 피드백 조회와 변경 API. */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
@PreAuthorize("principal.status == T(kr.ac.pusan.feedback.common.enums.UserStatus).ACTIVE")
public class AdminFeedbackController {

	private final FeedbackRepository feedbackRepository;
	private final AnswerRepository answerRepository;
	private final UserRepository userRepository;

	@Operation(summary = "관리용 피드백 목록 조회",
			description = "필터는 사이트·상태·유형·기간·회원 여부·답변 여부이고, 우선 처리 요청을 항상 먼저 정렬한다.")
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
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		validatePage(page, size);
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
		}

		List<Feedback> all = feedbackRepository.findAllWithAssociations();
		Map<Long, Answer> answers = answersByFeedback(all);
		Comparator<Feedback> order = feedbackOrder(sort == null ? FeedbackSort.PRIORITY : sort);
		List<Feedback> filtered = all.stream()
				.filter(feedback -> project == null || project.isBlank() || feedback.getProject().getCode().equals(project))
				.filter(feedback -> status == null || feedback.getStatus() == status)
				.filter(feedback -> category == null || feedback.getCategory() == category)
				.filter(feedback -> authorType == null || feedback.getAuthorType() == authorType)
				.filter(feedback -> from == null || !feedback.getCreatedAt().toLocalDate().isBefore(from))
				.filter(feedback -> to == null || !feedback.getCreatedAt().toLocalDate().isAfter(to))
				.filter(feedback -> answered == null || answers.containsKey(feedback.getId()) == answered)
				.sorted(order)
				.toList();

		int start = Math.min(page * size, filtered.size());
		int end = Math.min(start + size, filtered.size());
		List<AdminFeedbackSummary> items = filtered.subList(start, end).stream()
				.map(feedback -> FeedbackMapper.adminSummary(feedback, answers.containsKey(feedback.getId())))
				.toList();
		return new AdminFeedbackPage(items, filtered.size());
	}

	@Operation(summary = "관리용 피드백 상세 조회")
	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public AdminFeedbackDetail feedback(@PathVariable Long id) {
		Feedback feedback = findFeedback(id);
		return FeedbackMapper.adminDetail(feedback, answerRepository.findByFeedbackId(id).orElse(null));
	}

	@Operation(summary = "상태·유형·중요도 변경",
			description = "개발자 전용이다. 보낸 필드만 바꾼다. 작성자가 고른 reportedPriority 는 바꿀 수 없다.")
	@PatchMapping("/{id}")
	@Transactional
	public AdminFeedbackDetail update(@PathVariable Long id, @RequestBody FeedbackUpdateRequest request) {
		Feedback feedback = findFeedback(id);
		feedback.update(request.status(), request.category(), request.priority(), request.assigneeName(), LocalDateTime.now());
		return FeedbackMapper.adminDetail(feedback, answerRepository.findByFeedbackId(id).orElse(null));
	}

	@Operation(summary = "답변 등록·수정",
			description = "개발자 전용이다. 피드백 1건당 답변 1건이다. markDone 이 true 면 상태를 DONE 으로 함께 바꾼다.")
	@PutMapping("/{id}/answer")
	@Transactional
	public AnswerResponse upsertAnswer(
			@PathVariable Long id,
			@Valid @RequestBody AnswerUpsertRequest request,
			@CurrentUser AppUserPrincipal currentUser
	) {
		Feedback feedback = findFeedback(id);
		if (feedback.getAuthorType() == AuthorType.GUEST) {
			throw new IllegalArgumentException("비회원 피드백에는 답변을 등록할 수 없습니다.");
		}
		User author = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new NoSuchElementException("로그인 계정을 찾을 수 없습니다."));
		LocalDateTime now = LocalDateTime.now();
		Answer answer = answerRepository.findByFeedbackId(id).orElse(null);
		if (answer == null) {
			answer = answerRepository.save(Answer.builder()
					.feedback(feedback)
					.author(author)
					.content(request.content().trim())
					.createdAt(now)
					.updatedAt(now)
					.build());
			feedback.markAnswered(now);
		} else {
			answer.updateContent(request.content().trim(), now);
		}
		if (request.markDone()) {
			feedback.markDone(now);
		}
		return FeedbackMapper.answer(answer);
	}

	@Operation(summary = "우선 처리 요청 토글",
			description = "열람자 전용이다. 요청하면 중요도가 HIGH 로 올라가고 목록 최상단에 고정된다. 다시 호출하면 해제된다.")
	@PostMapping("/{id}/priority-request")
	@Transactional
	public PriorityRequestResponse priorityRequest(
			@PathVariable Long id,
			@CurrentUser AppUserPrincipal currentUser
	) {
		Feedback feedback = findFeedback(id);
		User requester = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new NoSuchElementException("로그인 계정을 찾을 수 없습니다."));
		feedback.togglePriorityRequest(requester);
		return new PriorityRequestResponse(feedback.getId(), feedback.isPriorityRequested(), feedback.getPriority());
	}

	private Feedback findFeedback(Long id) {
		return feedbackRepository.findByIdWithAssociations(id)
				.orElseThrow(() -> new NoSuchElementException("피드백을 찾을 수 없습니다."));
	}

	private Map<Long, Answer> answersByFeedback(List<Feedback> feedbacks) {
		if (feedbacks.isEmpty()) {
			return Map.of();
		}
		return answerRepository.findAllByFeedbackIdIn(feedbacks.stream().map(Feedback::getId).toList())
				.stream()
				.collect(Collectors.toMap(answer -> answer.getFeedback().getId(), Function.identity()));
	}

	private Comparator<Feedback> feedbackOrder(FeedbackSort sort) {
		return (left, right) -> {
			int requested = Boolean.compare(right.isPriorityRequested(), left.isPriorityRequested());
			if (requested != 0) {
				return requested;
			}
			if (sort == FeedbackSort.PRIORITY) {
				int priority = Integer.compare(priorityRank(left.getPriority()), priorityRank(right.getPriority()));
				if (priority != 0) {
					return priority;
				}
			}
			return sort == FeedbackSort.OLDEST
					? left.getCreatedAt().compareTo(right.getCreatedAt())
					: right.getCreatedAt().compareTo(left.getCreatedAt());
		};
	}

	private int priorityRank(Priority priority) {
		return switch (priority) {
			case HIGH -> 0;
			case NORMAL -> 1;
			case LOW -> 2;
		};
	}

	private void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("page는 0 이상, size는 1~100이어야 합니다.");
		}
	}
}
