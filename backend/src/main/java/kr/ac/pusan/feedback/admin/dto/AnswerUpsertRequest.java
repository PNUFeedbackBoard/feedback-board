package kr.ac.pusan.feedback.admin.dto;

/**
 * 답변 등록·수정 요청. PUT /api/admin/feedbacks/{id}/answer
 *
 * <p>답변은 피드백 1건당 1건이므로 등록과 수정을 같은 요청으로 처리한다(기획안 6-4).
 * markDone 이 true 면 답변과 함께 상태를 DONE 으로 바꾼다.
 */
public record AnswerUpsertRequest(
		String content,
		boolean markDone
) {

	// TODO(A, 3단계): 실제 구현 시 content 길이 제약(@NotBlank, @Size)을 이 자리에 붙이고
	//                 컨트롤러 파라미터에 @Valid 를 추가한다. 0-1단계 스텁은 본문을 읽지 않는다.
}
