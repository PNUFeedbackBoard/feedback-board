package kr.ac.pusan.feedback.config;

import java.util.ArrayList;
import java.util.List;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.AppUser;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import kr.ac.pusan.feedback.domain.FeedbackType;
import kr.ac.pusan.feedback.domain.Priority;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.Role;
import kr.ac.pusan.feedback.repository.AnswerRepository;
import kr.ac.pusan.feedback.repository.AppUserRepository;
import kr.ac.pusan.feedback.repository.FeedbackRepository;
import kr.ac.pusan.feedback.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class DevDataInitializer {

	private static final List<ProjectSeed> PROJECTS = List.of(
			new ProjectSeed("codeplace", "코드플레이스"),
			new ProjectSeed("aipms", "AIPMS"),
			new ProjectSeed("aicms", "AICMS"),
			new ProjectSeed("aicap", "AI역량지원시스템"),
			new ProjectSeed("srvadm", "pickle (서버관리)")
	);

	@Bean
	CommandLineRunner seedDevelopmentData(
			ProjectRepository projectRepository,
			AppUserRepository userRepository,
			FeedbackRepository feedbackRepository,
			AnswerRepository answerRepository
	) {
		return arguments -> {
			if (projectRepository.count() > 0) {
				return;
			}

			List<Project> projects = PROJECTS.stream()
					.map(seed -> new Project(seed.code(), seed.name()))
					.map(projectRepository::save)
					.toList();

			AppUser developer = userRepository.save(
					new AppUser("dev@demo.local", "데모 개발자", Role.DEVELOPER, true));
			userRepository.save(new AppUser("viewer@demo.local", "데모 열람자", Role.VIEWER, true));
			AppUser user = userRepository.save(
					new AppUser("user@demo.local", "데모 사용자", Role.USER, true));

			FeedbackStatus[] statuses = FeedbackStatus.values();
			FeedbackType[] types = FeedbackType.values();
			Priority[] priorities = Priority.values();
			List<Feedback> feedbacks = new ArrayList<>();

			for (int index = 0; index < 40; index++) {
				Feedback feedback = new Feedback(
						projects.get(index % projects.size()),
						index % 4 == 0 ? null : user,
						"데모 피드백 " + (index + 1),
						"화면 개발과 API 연동에 사용하는 시드 피드백 내용입니다. 항목 " + (index + 1),
						types[index % types.length],
						statuses[index % statuses.length],
						priorities[index % priorities.length],
						priorities[(index + 1) % priorities.length],
						index % 11 == 0
				);
				feedbacks.add(feedbackRepository.save(feedback));
			}

			feedbacks.stream()
					.filter(feedback -> feedback.getStatus() == FeedbackStatus.DONE)
					.forEach(feedback -> answerRepository.save(new Answer(
							feedback,
							developer,
							"요청하신 내용을 확인해 반영했습니다."
					)));
		};
	}

	private record ProjectSeed(String code, String name) {
	}
}
