package kr.ac.pusan.feedback.feedback;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.pusan.feedback.auth.AppUserPrincipal;
import kr.ac.pusan.feedback.auth.CurrentUser;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import kr.ac.pusan.feedback.feedback.dto.FeedbackCreateRequest;
import kr.ac.pusan.feedback.feedback.dto.FeedbackCreateResponse;

/**
 * 피드백 등록 API. 기획안 9장.
 *
 * <p>1단계에 A 가 실제 등록으로 교체했다.
 */
@Tag(name = "공개", description = "로그인 없이 호출할 수 있는 API")
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

	private final FeedbackRepository feedbackRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;

	FeedbackController(
			FeedbackRepository feedbackRepository,
			ProjectRepository projectRepository,
			UserRepository userRepository) {
		this.feedbackRepository = feedbackRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
	}

	/**
	 * 회원과 비회원 모두 등록할 수 있다. 작성자 정보는 본문으로 받지 않고 서버가 세션에서 판단한다.
	 *
	 * <p>{@code currentUser} 는 공개 경로라 로그인 안 했으면 null 이다(CurrentUser 주석 참고).
	 * null 이면 비회원(GUEST) 등록, 값이 있으면 그 계정을 작성자로 하는 회원(MEMBER) 등록이다.
	 */
	@Operation(summary = "피드백 등록",
			description = "회원과 비회원 모두 등록할 수 있다. 작성자 정보는 본문으로 받지 않고 서버가 세션에서 판단한다.")
	@PostMapping
	public FeedbackCreateResponse create(
			@Valid @RequestBody FeedbackCreateRequest request,
			@CurrentUser AppUserPrincipal currentUser) {
		Project project = projectRepository.findByCode(request.projectCode())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 사이트입니다."));

		User author = null;
		AuthorType authorType = AuthorType.GUEST;
		if (currentUser != null) {
			// AppUserPrincipal 은 세션용으로 값만 복사해 둔 것이라, 연관관계에 쓸 진짜 User 엔티티를
			// 다시 조회한다(AppUserPrincipal 클래스 설명 참고).
			author = userRepository.findByEmail(currentUser.getEmail())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
			authorType = AuthorType.MEMBER;
		}

		Feedback feedback = Feedback.create(project, author, authorType, request.title(), request.content(),
				request.category(), request.reportedPriority(), LocalDateTime.now());
		feedback = feedbackRepository.save(feedback);

		return new FeedbackCreateResponse(feedback.getId());
	}
}
