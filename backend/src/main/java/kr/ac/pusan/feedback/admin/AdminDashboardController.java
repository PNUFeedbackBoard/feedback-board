package kr.ac.pusan.feedback.admin;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackSummary;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse.CategoryCount;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse.DailyCount;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse.ProjectStatusCount;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse.UserCount;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;
import kr.ac.pusan.feedback.feedback.FeedbackMapper;
import lombok.RequiredArgsConstructor;

/** 전체 현황을 현재 DB 데이터에서 집계한다. */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("principal.status == T(kr.ac.pusan.feedback.common.enums.UserStatus).ACTIVE")
public class AdminDashboardController {

	private final FeedbackRepository feedbackRepository;
	private final ProjectRepository projectRepository;
	private final AnswerRepository answerRepository;

	@Operation(summary = "전체 현황 조회",
			description = "상태·프로젝트·유형·30일 추이·처리 시간·이용자·우선 요청·최근 접수를 집계한다.")
	@GetMapping
	@Transactional(readOnly = true)
	public DashboardResponse dashboard() {
		List<Feedback> feedbacks = feedbackRepository.findAllWithAssociations();
		Set<Long> answeredIds = new HashSet<>(answerRepository.findAll().stream()
				.map(answer -> answer.getFeedback().getId()).toList());
		long total = feedbacks.size();

		Map<FeedbackStatus, Long> statusCounts = emptyStatusCounts();
		feedbacks.forEach(feedback -> statusCounts.compute(feedback.getStatus(), (key, count) -> count + 1));

		List<ProjectStatusCount> projectStatusCounts = projectRepository.findAllByOrderBySortOrderAsc().stream()
				.map(project -> new ProjectStatusCount(project.getCode(), project.getName(),
						countsForProject(project, feedbacks)))
				.toList();

		List<CategoryCount> categoryCounts = Arrays.stream(FeedbackCategory.values())
				.map(category -> {
					long count = feedbacks.stream().filter(item -> item.getCategory() == category).count();
					return new CategoryCount(category, count, total == 0 ? 0 : (double) count / total);
				})
				.toList();

		LocalDate today = LocalDate.now();
		List<DailyCount> dailyTrend = IntStream.rangeClosed(0, 29)
				.mapToObj(offset -> today.minusDays(29L - offset))
				.map(day -> new DailyCount(day.toString(), feedbacks.stream()
						.filter(item -> item.getCreatedAt().toLocalDate().equals(day)).count()))
				.toList();

		double averageProcessingHours = feedbacks.stream()
				.filter(item -> item.getClosedAt() != null)
				.mapToLong(item -> Duration.between(item.getCreatedAt(), item.getClosedAt()).toMinutes())
				.average()
				.orElse(0) / 60.0;

		long memberCount = feedbacks.stream()
				.filter(item -> item.getAuthor() != null)
				.map(item -> item.getAuthor().getId())
				.distinct()
				.count();
		long guestCount = feedbacks.stream().filter(item -> item.getAuthorType() == AuthorType.GUEST).count();

		List<AdminFeedbackSummary> priorityRequested = feedbacks.stream()
				.filter(Feedback::isPriorityRequested)
				.sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
				.map(item -> FeedbackMapper.adminSummary(item, answeredIds.contains(item.getId())))
				.toList();

		List<AdminFeedbackSummary> recent = feedbacks.stream()
				.sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
				.limit(10)
				.map(item -> FeedbackMapper.adminSummary(item, answeredIds.contains(item.getId())))
				.toList();

		return new DashboardResponse(statusCounts, projectStatusCounts, categoryCounts, dailyTrend,
				averageProcessingHours, new UserCount(memberCount, guestCount), priorityRequested, recent);
	}

	private Map<FeedbackStatus, Long> countsForProject(Project project, List<Feedback> feedbacks) {
		Map<FeedbackStatus, Long> counts = emptyStatusCounts();
		feedbacks.stream()
				.filter(item -> item.getProject().getId().equals(project.getId()))
				.forEach(item -> counts.compute(item.getStatus(), (key, count) -> count + 1));
		return counts;
	}

	private Map<FeedbackStatus, Long> emptyStatusCounts() {
		Map<FeedbackStatus, Long> counts = new EnumMap<>(FeedbackStatus.class);
		Arrays.stream(FeedbackStatus.values()).forEach(status -> counts.put(status, 0L));
		return counts;
	}
}
