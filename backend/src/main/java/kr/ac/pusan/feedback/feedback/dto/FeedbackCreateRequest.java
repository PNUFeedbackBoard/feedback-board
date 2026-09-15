package kr.ac.pusan.feedback.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 피드백 등록 요청. POST /api/feedbacks
 *
 * <p>제약은 기획안 5-3의 작성 화면 표와 같다. 검증 실패는 GlobalExceptionHandler 가
 * 공통 오류 형식(ApiErrorResponse)으로 변환한다.
 *
 * <p>작성자 정보는 요청 본문으로 받지 않는다. 회원 여부와 작성자는 서버가 세션에서 판단한다.
 * projectCode 는 진입 링크의 ?site= 값과 같은 Project.code 다.
 */
public record FeedbackCreateRequest(

		@NotBlank(message = "사이트를 선택해 주세요.")
		String projectCode,

		@NotNull(message = "유형을 선택해 주세요.")
		FeedbackCategory category,

		@NotNull(message = "긴급도를 선택해 주세요.")
		Priority reportedPriority,

		@NotBlank(message = "제목을 입력해 주세요.")
		@Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
		String title,

		@NotBlank(message = "내용을 입력해 주세요.")
		@Size(min = 10, max = 2000, message = "내용은 10자 이상 2,000자 이하로 입력해 주세요.")
		String content
) {
}
