package kr.ac.pusan.feedback.feedback;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.common.StubData;
import kr.ac.pusan.feedback.feedback.dto.MeResponse;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackDetail;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackSummary;

/**
 * 내 계정과 내 문의 API. 기획안 5-3, 9장.
 *
 * <p>0-1단계 스텁이다. 세션을 보지 않고 데모 계정 user@demo.local 의 데이터를 고정으로 돌려준다.
 * 화면은 역할을 스스로 판단하지 말고 /api/me 의 role 값만 사용한다(기획안 13-2).
 */
@Tag(name = "회원", description = "로그인한 사용자용 API")
@RestController
@RequestMapping("/api/me")
public class MeController {

	// TODO(B, 1단계): 세션에서 현재 계정을 읽어 돌려주도록 교체한다.
	//                 로그인하지 않았으면 401 을 반환한다. 7단계에서 구글 로그인으로 바뀌어도 응답 형태는 그대로다.
	@Operation(summary = "내 계정 조회",
			description = "화면이 역할과 계정 상태를 판단하는 유일한 근거다. 화면에서 역할을 임의로 정하지 않는다.")
	@GetMapping
	public MeResponse me() {
		return StubData.me();
	}

	// TODO(A, 3단계): 세션 계정이 등록한 피드백만 최신순으로 조회하도록 교체한다.
	@Operation(summary = "내 문의 목록 조회",
			description = "본인이 등록한 피드백만 최신순으로 내려준다. 비회원은 조회할 수 없다.")
	@GetMapping("/feedbacks")
	public List<MyFeedbackSummary> myFeedbacks() {
		return StubData.myFeedbacks();
	}

	// TODO(A, 3단계): 실제 조회로 교체한다. 본인 피드백이 아니면 404 를 반환한다.
	//                 답변이 없으면 answer 를 null 로 내려준다.
	@Operation(summary = "내 문의 상세 조회",
			description = "본인이 등록한 피드백만 조회할 수 있다. 답변이 없으면 answer 가 null 이다.")
	@GetMapping("/feedbacks/{id}")
	public MyFeedbackDetail myFeedbackDetail(@PathVariable Long id) {
		return StubData.myFeedbackDetail(id);
	}
}
