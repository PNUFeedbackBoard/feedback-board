package kr.ac.pusan.feedback.feedback;

import kr.ac.pusan.feedback.admin.dto.AdminFeedbackDetail;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackSummary;
import kr.ac.pusan.feedback.admin.dto.AdminUserResponse;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackDetail;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackSummary;
import kr.ac.pusan.feedback.feedback.dto.ProjectResponse;

/** 엔티티가 영속성 계층 밖으로 새지 않도록 API 응답 변환을 한곳에 모은다. */
public final class FeedbackMapper {

	private FeedbackMapper() {
	}

	public static ProjectResponse project(Project project) {
		return new ProjectResponse(project.getId(), project.getCode(), project.getName(),
				project.getLogoUrl(), project.getSiteUrl(), project.getSortOrder());
	}

	public static MyFeedbackSummary mySummary(Feedback feedback, boolean answered) {
		return new MyFeedbackSummary(feedback.getId(), feedback.getProject().getCode(),
				feedback.getProject().getName(), feedback.getTitle(), feedback.getCategory(),
				feedback.getStatus(), feedback.getCreatedAt(), answered);
	}

	public static MyFeedbackDetail myDetail(Feedback feedback, Answer answer) {
		MyFeedbackDetail.AnswerView answerView = answer == null ? null : new MyFeedbackDetail.AnswerView(
				answer.getContent(), answer.getCreatedAt(), answer.getUpdatedAt());
		return new MyFeedbackDetail(feedback.getId(), feedback.getProject().getCode(),
				feedback.getProject().getName(), feedback.getTitle(), feedback.getContent(),
				feedback.getCategory(), feedback.getStatus(), feedback.getReportedPriority(),
				feedback.getCreatedAt(), answerView);
	}

	public static AdminFeedbackSummary adminSummary(Feedback feedback, boolean answered) {
		return new AdminFeedbackSummary(feedback.getId(), feedback.getProject().getCode(),
				feedback.getProject().getName(), feedback.getTitle(), feedback.getCategory(),
				feedback.getStatus(), feedback.getReportedPriority(), feedback.getPriority(),
				feedback.isPriorityRequested(), feedback.getAuthorType(), authorName(feedback),
				feedback.getAssigneeName(), feedback.getCreatedAt(), answered);
	}

	public static AdminFeedbackDetail adminDetail(Feedback feedback, Answer answer) {
		return new AdminFeedbackDetail(feedback.getId(), feedback.getProject().getCode(),
				feedback.getProject().getName(), feedback.getTitle(), feedback.getCategory(),
				feedback.getStatus(), feedback.getReportedPriority(), feedback.getPriority(),
				feedback.isPriorityRequested(), feedback.getAuthorType(), authorName(feedback),
				feedback.getAssigneeName(), feedback.getCreatedAt(), answer != null, feedback.getContent(),
				feedback.getFirstAnsweredAt(), feedback.getClosedAt(),
				answer == null ? null : answer(answer));
	}

	public static AnswerResponse answer(Answer answer) {
		return new AnswerResponse(answer.getId(), answer.getContent(), answer.getCreatedAt(), answer.getUpdatedAt());
	}

	public static AdminUserResponse user(User user) {
		return new AdminUserResponse(user.getId(), user.getEmail(), user.getName(),
				user.getRole(), user.getStatus(), user.getCreatedAt());
	}

	private static String authorName(Feedback feedback) {
		return feedback.getAuthor() == null ? null : feedback.getAuthor().getName();
	}
}
