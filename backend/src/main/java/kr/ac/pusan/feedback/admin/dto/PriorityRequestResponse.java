package kr.ac.pusan.feedback.admin.dto;

import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 우선 처리 요청 토글 결과. POST /api/admin/feedbacks/{id}/priority-request
 *
 * <p>요청하면 priorityRequested 가 true 가 되고 priority 는 HIGH 로 올라간다(기획안 4-4).
 * 다시 호출하면 해제되며, 해제는 요청한 열람자 본인만 할 수 있다.
 */
public record PriorityRequestResponse(
		Long id,
		boolean priorityRequested,
		Priority priority
) {
}
