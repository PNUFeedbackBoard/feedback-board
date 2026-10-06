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
 * DEVELOPER·ACTIVE 계정이 아니면 400 이다.
 *
 * <p>assigneeId 가 null 이면 다른 세 필드와 같은 규칙으로 "담당자를 바꾸지 않는다"는 뜻이다.
 * 그래서 "담당자를 없앤다(해제)"는 별도로 표현할 방법이 필요해 {@code unassign} 을 추가했다.
 * {@code unassign} 이 true 면 assigneeId 를 무시하고 담당자를 null 로 비운다.
 * 즉 "지정"은 assigneeId 만 보내고, "해제"는 unassign 을 true 로 보낸다. 두 값을 같이 보내면
 * unassign 이 우선한다(Feedback#applyAdminUpdate 참고).
 */
public record FeedbackUpdateRequest(
		FeedbackStatus status,
		FeedbackCategory category,
		Priority priority,
		Long assigneeId,
		boolean unassign
) {
}
