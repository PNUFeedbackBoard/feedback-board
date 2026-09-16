package kr.ac.pusan.feedback.feedback.dto;

import java.time.LocalDateTime;

/**
 * 개발자 답변 응답. PUT /api/admin/feedbacks/{id}/answer 의 응답이며
 * AdminFeedbackDetail 의 answer 필드에도 같은 형태로 들어간다.
 *
 * <p>사용자용 상세(MyFeedbackDetail)의 answer 는 id 를 내보내지 않는 별도 형태
 * (MyFeedbackDetail.AnswerView)를 쓴다. 사용자에게 내부 식별자를 노출하지 않기 위한 구분이다.
 */
public record AnswerResponse(
		Long id,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
