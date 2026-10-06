package kr.ac.pusan.feedback.admin;

import kr.ac.pusan.feedback.admin.dto.AdminFeedbackDetail;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackSummary;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;

/**
 * Feedback 엔티티 → 관리용 응답 DTO 변환. B 가 추가했다.
 *
 * <p>{@link AdminFeedbackController}(목록·상세·변경)와 {@link AdminDashboardController}(우선 처리
 * 요청·최근 접수 목록)가 둘 다 같은 모양(AdminFeedbackSummary)으로 변환해야 해서, 중복을 피하려고
 * 공용 클래스로 뺐다. 순수 변환 함수만 모아 둔다 — DB 조회는 하지 않는다(answered 여부처럼 조회가
 * 필요한 값은 호출자가 미리 구해서 인자로 넘긴다).
 */
final class AdminFeedbackMapper {

	private AdminFeedbackMapper() {
	}

	static AdminFeedbackSummary toSummary(Feedback feedback, boolean answered) {
		return new AdminFeedbackSummary(
				feedback.getId(),
				feedback.getProject().getCode(),
				feedback.getProject().getName(),
				feedback.getTitle(),
				feedback.getCategory(),
				feedback.getStatus(),
				feedback.getReportedPriority(),
				feedback.getPriority(),
				feedback.isPriorityRequested(),
				feedback.getAuthorType(),
				authorNameOf(feedback),
				feedback.getCreatedAt(),
				answered,
				feedback.getAssignee() == null ? null : feedback.getAssignee().getId(),
				feedback.getAssignee() == null ? null : feedback.getAssignee().getName());
	}

	static AdminFeedbackDetail toDetail(Feedback feedback, Answer answer) {
		AnswerResponse answerResponse = answer == null ? null
				: new AnswerResponse(answer.getId(), answer.getContent(), answer.getCreatedAt(), answer.getUpdatedAt());

		return new AdminFeedbackDetail(
				feedback.getId(),
				feedback.getProject().getCode(),
				feedback.getProject().getName(),
				feedback.getTitle(),
				feedback.getCategory(),
				feedback.getStatus(),
				feedback.getReportedPriority(),
				feedback.getPriority(),
				feedback.isPriorityRequested(),
				feedback.getAuthorType(),
				authorNameOf(feedback),
				feedback.getCreatedAt(),
				answer != null,
				feedback.getContent(),
				feedback.getFirstAnsweredAt(),
				feedback.getClosedAt(),
				answerResponse,
				feedback.getAssignee() == null ? null : feedback.getAssignee().getId(),
				feedback.getAssignee() == null ? null : feedback.getAssignee().getName());
	}

	private static String authorNameOf(Feedback feedback) {
		return feedback.getAuthorType() == AuthorType.GUEST ? null : feedback.getAuthor().getName();
	}
}
