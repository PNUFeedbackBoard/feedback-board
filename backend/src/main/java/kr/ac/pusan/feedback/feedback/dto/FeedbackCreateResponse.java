package kr.ac.pusan.feedback.feedback.dto;

/**
 * 피드백 등록 응답. POST /api/feedbacks (201)
 *
 * <p>등록한 피드백의 식별자만 내려준다. 비회원은 이 값으로도 다시 조회할 수 없다(기획안 10장).
 */
public record FeedbackCreateResponse(
		Long id
) {
}
