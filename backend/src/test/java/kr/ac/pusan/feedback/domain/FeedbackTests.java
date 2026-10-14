package kr.ac.pusan.feedback.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;

class FeedbackTests {

	@Test
	void reopeningClearsClosedAtAndClosingAgainRecordsNewTime() {
		Project project = Project.builder().code("test").name("테스트").sortOrder(1).build();
		LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 9, 0);
		Feedback feedback = Feedback.create(project, null, AuthorType.GUEST, "제목", "내용",
				FeedbackCategory.BUG, Priority.NORMAL, createdAt);
		LocalDateTime firstClosedAt = createdAt.plusHours(2);

		feedback.applyAdminUpdate(FeedbackStatus.DONE, null, null, null, false, firstClosedAt);
		assertThat(feedback.getClosedAt()).isEqualTo(firstClosedAt);

		feedback.applyAdminUpdate(FeedbackStatus.REJECTED, null, null, null, false, createdAt.plusHours(3));
		assertThat(feedback.getClosedAt()).isEqualTo(firstClosedAt);

		feedback.applyAdminUpdate(FeedbackStatus.IN_PROGRESS, null, null, null, false, createdAt.plusHours(4));
		assertThat(feedback.getClosedAt()).isNull();

		LocalDateTime secondClosedAt = createdAt.plusHours(6);
		feedback.applyAdminUpdate(FeedbackStatus.DONE, null, null, null, false, secondClosedAt);
		assertThat(feedback.getClosedAt()).isEqualTo(secondClosedAt);
	}
}
