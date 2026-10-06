package kr.ac.pusan.feedback.admin.dto;

import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 상태·유형·중요도 변경 요청. PATCH /api/admin/feedbacks/{id}
 *
 * <p>네 필드 모두 nullable 이며, 보낸 값만 바꾼다. null 인 필드는 변경하지 않는다.
 * 여기의 priority 는 개발자 확정값이며, 작성자가 고른 reportedPriority 는 바꿀 수 없다(기획안 4-3).
 * VIEWER 가 호출하면 403 이다(기획안 9장).
 *
 * <p>assigneeId 는 기획에 없던 기능이다(AssigneeDialog.jsx 주석, docs/planning.md 8·9장 참고).
 * DEVELOPER·ACTIVE 계정이 아니면 400 이다. 지금은 "지정"만 되고 "해제"는 지원하지 않는다
 * (null 을 보내면 다른 필드와 마찬가지로 "변경 안 함"으로 취급되기 때문 — Feedback#applyAdminUpdate 참고).
 */
public record FeedbackUpdateRequest(
		FeedbackStatus status,
		FeedbackCategory category,
		Priority priority,
		Long assigneeId
) {
}
