package kr.ac.pusan.feedback.admin.dto;

import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 상태·유형·중요도 변경 요청. PATCH /api/admin/feedbacks/{id}
 *
 * <p>세 필드 모두 nullable 이며, 보낸 값만 바꾼다. null 인 필드는 변경하지 않는다.
 * 여기의 priority 는 개발자 확정값이며, 작성자가 고른 reportedPriority 는 바꿀 수 없다(기획안 4-3).
 * VIEWER 가 호출하면 403 이다(기획안 9장).
 */
public record FeedbackUpdateRequest(
		FeedbackStatus status,
		FeedbackCategory category,
		Priority priority
) {
}
