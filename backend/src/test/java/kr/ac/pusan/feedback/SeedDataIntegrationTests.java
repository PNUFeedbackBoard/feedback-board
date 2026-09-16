package kr.ac.pusan.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;
import kr.ac.pusan.feedback.repository.AppUserRepository;
import kr.ac.pusan.feedback.repository.FeedbackRepository;
import kr.ac.pusan.feedback.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class SeedDataIntegrationTests {

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	AppUserRepository userRepository;

	@Autowired
	FeedbackRepository feedbackRepository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void createsFourDomainTablesAndDevelopmentSeedData() {
		Integer tableCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_SCHEMA = 'PUBLIC'
				  AND TABLE_NAME IN ('PROJECTS', 'APP_USERS', 'FEEDBACKS', 'ANSWERS')
				""", Integer.class);

		assertThat(tableCount).isEqualTo(4);
		assertThat(projectRepository.count()).isEqualTo(5);
		assertThat(userRepository.count()).isEqualTo(3);
		assertThat(feedbackRepository.count()).isEqualTo(40);
	}

	@Test
	void distributesSeedFeedbackAcrossEveryEnumValue() {
		var feedbacks = feedbackRepository.findAll();

		assertThat(countBy(feedbacks, Feedback::getStatus))
				.containsExactlyInAnyOrderEntriesOf(Map.of(
						FeedbackStatus.RECEIVED, 10L,
						FeedbackStatus.IN_PROGRESS, 10L,
						FeedbackStatus.DONE, 10L,
						FeedbackStatus.REJECTED, 10L
				));
		assertThat(countBy(feedbacks, Feedback::getType)).containsKeys(FeedbackType.values());
		assertThat(countBy(feedbacks, Feedback::getReportedPriority)).containsKeys(Priority.values());
		assertThat(countBy(feedbacks, Feedback::getPriority)).containsKeys(Priority.values());
	}

	private <T> Map<T, Long> countBy(java.util.List<Feedback> feedbacks, Function<Feedback, T> classifier) {
		return feedbacks.stream().collect(Collectors.groupingBy(classifier, Collectors.counting()));
	}
}
