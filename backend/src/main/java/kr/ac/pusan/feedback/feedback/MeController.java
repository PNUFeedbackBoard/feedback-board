package kr.ac.pusan.feedback.feedback;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.auth.AppUserPrincipal;
import kr.ac.pusan.feedback.auth.CurrentUser;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.feedback.dto.MeResponse;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackDetail;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackSummary;
import lombok.RequiredArgsConstructor;

/**
 * 내 계정과 내 문의 API. 기획안 5-3, 9장.
 *
 * <p>세션의 현재 계정으로 본인 데이터만 조회한다.
 */
@Tag(name = "회원", description = "로그인한 사용자용 API")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

	private final FeedbackRepository feedbackRepository;
	private final AnswerRepository answerRepository;

	@Operation(hidden = true, summary = "내 계정 조회",
			description = "화면이 역할과 계정 상태를 판단하는 유일한 근거다. 화면에서 역할을 임의로 정하지 않는다.")
	@GetMapping
	public MeResponse me(@CurrentUser AppUserPrincipal currentUser) {
		return new MeResponse(
				currentUser.getId(),
				currentUser.getEmail(),
				currentUser.getName(),
				currentUser.getRole(),
				currentUser.getStatus()
		);
	}

	@Operation(summary = "내 문의 목록 조회",
			description = "본인이 등록한 피드백만 최신순으로 내려준다. 비회원은 조회할 수 없다.")
	@GetMapping("/feedbacks")
	@Transactional(readOnly = true)
	public List<MyFeedbackSummary> myFeedbacks(@CurrentUser AppUserPrincipal currentUser) {
		List<Feedback> feedbacks = feedbackRepository.findAllByAuthorIdOrderByCreatedAtDesc(currentUser.getId());
		Map<Long, Answer> answers = answersByFeedback(feedbacks);
		return feedbacks.stream()
				.map(feedback -> FeedbackMapper.mySummary(feedback, answers.containsKey(feedback.getId())))
				.toList();
	}

	@Operation(summary = "내 문의 상세 조회",
			description = "본인이 등록한 피드백만 조회할 수 있다. 답변이 없으면 answer 가 null 이다.")
	@GetMapping("/feedbacks/{id}")
	@Transactional(readOnly = true)
	public MyFeedbackDetail myFeedbackDetail(
			@PathVariable Long id,
			@CurrentUser AppUserPrincipal currentUser
	) {
		Feedback feedback = feedbackRepository.findByIdAndAuthorId(id, currentUser.getId())
				.orElseThrow(() -> new NoSuchElementException("문의 내용을 찾을 수 없습니다."));
		Answer answer = answerRepository.findByFeedbackId(id).orElse(null);
		return FeedbackMapper.myDetail(feedback, answer);
	}

	private Map<Long, Answer> answersByFeedback(List<Feedback> feedbacks) {
		if (feedbacks.isEmpty()) {
			return Map.of();
		}
		return answerRepository.findAllByFeedbackIdIn(feedbacks.stream().map(Feedback::getId).toList())
				.stream()
				.collect(Collectors.toMap(answer -> answer.getFeedback().getId(), Function.identity()));
	}
}
