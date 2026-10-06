package kr.ac.pusan.feedback.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 답변 등록·수정 요청. PUT /api/admin/feedbacks/{id}/answer
 *
 * <p>답변은 피드백 1건당 1건이므로 등록과 수정을 같은 요청으로 처리한다(기획안 6-4).
 * markDone 이 true 면 답변과 함께 상태를 DONE 으로 바꾼다.
 *
 * <p>길이 제약은 피드백 본문(기획안 5-3)의 최대값만 맞췄다. 최소 길이는 두지 않았다 —
 * "확인했습니다"처럼 짧은 답도 정상적인 답변이라 10자 이상 같은 제약을 걸 이유가 없었다.
 */
public record AnswerUpsertRequest(

		@NotBlank(message = "답변 내용을 입력해 주세요.")
		@Size(max = 2000, message = "답변은 2,000자 이하로 입력해 주세요.")
		String content,

		boolean markDone
) {
}
