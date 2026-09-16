package kr.ac.pusan.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class SeedDataIntegrationTests {

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	FeedbackRepository feedbackRepository;

	@Autowired
	AnswerRepository answerRepository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void createsFourDomainTablesAndDevelopmentSeedData() {
		Integer tableCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_SCHEMA = 'PUBLIC'
				  AND TABLE_NAME IN ('PROJECTS', 'USERS', 'FEEDBACKS', 'ANSWERS')
				""", Integer.class);

		assertThat(tableCount).isEqualTo(4);
		assertThat(projectRepository.count()).isEqualTo(5);
		assertThat(userRepository.count()).isEqualTo(10);
		assertThat(feedbackRepository.count()).isEqualTo(40);
		assertThat(answerRepository.count()).isEqualTo(15);
	}

	@Test
	void distributesSeedFeedbackAcrossEveryEnumValue() {
		var feedbacks = feedbackRepository.findAll();

		assertThat(countBy(feedbacks, Feedback::getStatus))
				.containsExactlyInAnyOrderEntriesOf(Map.of(
						FeedbackStatus.RECEIVED, 12L,
						FeedbackStatus.IN_PROGRESS, 6L,
						FeedbackStatus.DONE, 18L,
						FeedbackStatus.REJECTED, 4L
				));
		assertThat(countBy(feedbacks, Feedback::getCategory)).containsKeys(FeedbackCategory.values());
		assertThat(countBy(feedbacks, Feedback::getReportedPriority)).containsKeys(Priority.values());
		assertThat(countBy(feedbacks, Feedback::getPriority)).containsKeys(Priority.values());
	}

	private <T> Map<T, Long> countBy(java.util.List<Feedback> feedbacks, Function<Feedback, T> classifier) {
		return feedbacks.stream().collect(Collectors.groupingBy(classifier, Collectors.counting()));
	}
}
