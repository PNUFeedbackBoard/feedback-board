package kr.ac.pusan.feedback.feedback;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.pusan.feedback.common.StubData;
import kr.ac.pusan.feedback.feedback.dto.FeedbackCreateRequest;
import kr.ac.pusan.feedback.feedback.dto.FeedbackCreateResponse;

/**
 * 피드백 등록 API. 기획안 9장.
 *
 * <p>0-1단계 스텁이다. 요청 본문 검증은 실제로 동작하지만, 저장은 하지 않고 고정 id 를 돌려준다.
 * 검증을 먼저 살려 두는 이유는 D 가 작성 화면에서 오류 표시를 바로 확인할 수 있게 하기 위해서다.
 */
@Tag(name = "공개", description = "로그인 없이 호출할 수 있는 API")
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

	// TODO(A, 1단계): 실제 등록으로 교체한다.
	//                 projectCode 로 Project 를 찾고, 세션이 있으면 author 와 AuthorType.MEMBER,
	//                 없으면 author=null 과 AuthorType.GUEST 로 Feedback.create 를 호출한다.
	@Operation(summary = "피드백 등록",
			description = "회원과 비회원 모두 등록할 수 있다. 작성자 정보는 본문으로 받지 않고 서버가 세션에서 판단한다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FeedbackCreateResponse create(@Valid @RequestBody FeedbackCreateRequest request) {
		return StubData.feedbackCreated();
	}
}
