package kr.ac.pusan.feedback.feedback;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import lombok.RequiredArgsConstructor;

/**
 * 피드백 등록 API. 기획안 9장.
 *
 * <p>세션이 있으면 회원, 없으면 비회원으로 판단해 DB에 저장한다.
 */
@Tag(name = "공개", description = "로그인 없이 호출할 수 있는 API")
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final FeedbackRepository feedbackRepository;

	@Operation(summary = "피드백 등록",
			description = "회원과 비회원 모두 등록할 수 있다. 작성자 정보는 본문으로 받지 않고 서버가 세션에서 판단한다.")
	@PostMapping
	public FeedbackCreateResponse create(
			@Valid @RequestBody FeedbackCreateRequest request,
			@CurrentUser AppUserPrincipal currentUser
	) {
		Project project = projectRepository.findByCode(request.projectCode())
				.orElseThrow(() -> new NoSuchElementException("선택한 프로젝트를 찾을 수 없습니다."));
		User author = currentUser == null ? null : userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new NoSuchElementException("로그인 계정을 찾을 수 없습니다."));
		AuthorType authorType = author == null ? AuthorType.GUEST : AuthorType.MEMBER;
		Feedback saved = feedbackRepository.save(Feedback.create(project, author, authorType,
				request.title().trim(), request.content().trim(), request.category(),
				request.reportedPriority(), LocalDateTime.now()));
		return new FeedbackCreateResponse(saved.getId());
	}
}
