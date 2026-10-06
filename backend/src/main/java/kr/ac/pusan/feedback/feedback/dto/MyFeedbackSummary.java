package kr.ac.pusan.feedback.feedback.dto;

import java.time.LocalDateTime;

import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 내 문의 목록 항목. GET /api/me/feedbacks
 *
 * <p>사용자용이므로 개발자가 확정한 priority·우선 처리 요청 여부·내부 타임스탬프는 포함하지 않는다(기획안 13-4).
 * 작성자 본인이 고른 reportedPriority 는 MyFeedbackDetail 과 동일하게 내보낸다 — 내부 정보가 아니라
 * 본인이 입력한 값을 되돌려 주는 것뿐이라 상세와 목록을 다르게 가릴 이유가 없다.
 *
 * TODO(B, 계약 변경): reportedPriority 필드 추가. 상세(MyFeedbackDetail)에는 이미 내려주고 있는데
 * 목록에는 빠져 있던 걸 B가 발견해 추가함. A에게 공유 필요 — docs/planning.md 13-4 표에도 반영할 것.
 */
public record MyFeedbackSummary(
		Long id,
		String projectCode,
		String projectName,
		String title,
		FeedbackCategory category,
		FeedbackStatus status,
		LocalDateTime createdAt,
		boolean answered,
		Priority reportedPriority
) {
}
