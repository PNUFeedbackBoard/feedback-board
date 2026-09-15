package kr.ac.pusan.feedback.admin.dto;

import java.time.LocalDateTime;

import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;

/**
 * 관리용 상세. GET/PATCH /api/admin/feedbacks/{id}
 *
 * <p>AdminFeedbackSummary 의 모든 필드에 content, firstAnsweredAt, closedAt, answer 를 더한 형태다.
 * record 는 상속할 수 없으므로 필드를 그대로 다시 선언한다. 필드 이름은 Summary 와 반드시 같아야 한다.
 *
 * <p>firstAnsweredAt 과 closedAt 은 대시보드의 평균 처리 소요 시간 산출에 쓰는 값이며,
 * 아직 발생하지 않았으면 null 이다. answer 는 답변이 없으면 null 이다.
 */
public record AdminFeedbackDetail(
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
		boolean answered,
		String content,
		LocalDateTime firstAnsweredAt,
		LocalDateTime closedAt,
		AnswerResponse answer
) {
}
