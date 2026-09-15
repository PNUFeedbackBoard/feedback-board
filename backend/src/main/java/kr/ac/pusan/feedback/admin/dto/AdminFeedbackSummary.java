package kr.ac.pusan.feedback.admin.dto;

import java.time.LocalDateTime;

import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;

/**
 * 관리용 목록 항목. GET /api/admin/feedbacks 의 items 와 대시보드의 목록 두 곳에서 쓴다.
 *
 * <p>사용자용 DTO 와 달리 작성자 정보, 중요도 2필드, 우선 처리 요청 여부를 포함한다(기획안 13-4).
 *
 * <ul>
 *   <li>reportedPriority 는 작성자가 고른 값이며 정렬에 쓰지 않는다.</li>
 *   <li>priority 는 개발자가 확정한 값이며 정렬 기준이다.</li>
 *   <li>authorName 은 authorType 이 GUEST 면 null 이다.</li>
 *   <li>authorType 이 GUEST 인 항목은 답변 대상이 아니다. 답변 탭에서 비활성으로 표시한다(기획안 6-4).</li>
 * </ul>
 */
public record AdminFeedbackSummary(
		Long id,
		String projectCode,
		String projectName,
		String title,
		FeedbackCategory category,
		FeedbackStatus status,
		Priority reportedPriority,
		Priority priority,
		boolean priorityRequested,
		AuthorType authorType,
		String authorName,
		LocalDateTime createdAt,
		boolean answered
) {
}
