package kr.ac.pusan.feedback.admin;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackSummary;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;

/**
 * 전체 현황 대시보드 API. 기획안 6-2, 9장.
 *
 * <p>3단계에 B 가 실제 집계로 교체했다. 지표 6종의 산출 방식은 기획안 6-2 표를 그대로 따른다.
 * 평균 처리 소요 시간과 기간별 추이는 DB 함수 대신 자바에서 계산한다(이유는
 * {@link FeedbackRepository} 의 "대시보드 집계" 구역 주석 참고 — H2·PostgreSQL 날짜 함수 차이 회피).
 * 데이터가 많아져 느려지면 5단계("대시보드 집계 쿼리 정리")에서 DB 쪽으로 옮긴다.
 */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

	/** 기간별 추이 기본 조회 범위(기획안 6-2) */
	private static final int TREND_DAYS = 30;

	/** 최근 접수 목록 건수(기획안 6-2) */
	private static final int RECENT_SIZE = 10;

	private final FeedbackRepository feedbackRepository;
	private final AnswerRepository answerRepository;
	private final ProjectRepository projectRepository;

	AdminDashboardController(
			FeedbackRepository feedbackRepository,
			AnswerRepository answerRepository,
			ProjectRepository projectRepository) {
		this.feedbackRepository = feedbackRepository;
		this.answerRepository = answerRepository;
		this.projectRepository = projectRepository;
	}

	@Operation(summary = "전체 현황 조회",
			description = "조회 전용 화면이다. 상태별 건수, 프로젝트별 교차 집계, 유형별 비율, 30일 추이, "
					+ "평균 처리 소요 시간, 이용자 수, 우선 처리 요청 목록, 최근 접수 10건을 한 번에 내려준다.")
	@GetMapping
	public DashboardResponse dashboard() {
		List<Project> projects = projectRepository.findAllByOrderBySortOrderAsc();

		return new DashboardResponse(
				statusCounts(),
				projectStatusCounts(projects),
				categoryCounts(),
				dailyTrend(),
				averageProcessingHours(),
				userCount(),
				toSummaries(feedbackRepository.findPriorityRequested()),
				toSummaries(feedbackRepository.findRecent(PageRequest.of(0, RECENT_SIZE))));
	}

	// ------------------------------------------------------------------
	// 지표 1 — 상태별 건수
	// ------------------------------------------------------------------

	private Map<FeedbackStatus, Long> statusCounts() {
		Map<FeedbackStatus, Long> counts = zeroFilledStatusCounts();
		for (FeedbackRepository.StatusCount row : feedbackRepository.countByStatus()) {
			counts.put(row.getStatus(), row.getCount());
		}
		return counts;
	}

	/** 네 가지 상태를 전부 0으로 채운다. 건수가 0인 상태는 GROUP BY 결과에 행 자체가 안 나오기 때문이다. */
	private static Map<FeedbackStatus, Long> zeroFilledStatusCounts() {
		Map<FeedbackStatus, Long> counts = new LinkedHashMap<>();
		for (FeedbackStatus status : FeedbackStatus.values()) {
			counts.put(status, 0L);
		}
		return counts;
	}

	// ------------------------------------------------------------------
	// 지표 2 — 프로젝트 × 상태
	// ------------------------------------------------------------------

	private List<DashboardResponse.ProjectStatusCount> projectStatusCounts(List<Project> projects) {
		// 프로젝트 code -> (상태별 건수, 전부 0으로 시작). 피드백이 0건인 프로젝트도 목록에 나와야 한다.
		Map<String, Map<FeedbackStatus, Long>> byProjectCode = new LinkedHashMap<>();
		for (Project project : projects) {
			byProjectCode.put(project.getCode(), zeroFilledStatusCounts());
		}
		for (FeedbackRepository.ProjectStatusCount row : feedbackRepository.countByProjectAndStatus()) {
			Map<FeedbackStatus, Long> counts = byProjectCode.get(row.getProjectCode());
			if (counts != null) {
				counts.put(row.getStatus(), row.getCount());
			}
		}

		List<DashboardResponse.ProjectStatusCount> result = new ArrayList<>();
		for (Project project : projects) {
			result.add(new DashboardResponse.ProjectStatusCount(
					project.getCode(), project.getName(), byProjectCode.get(project.getCode())));
		}
		return result;
	}

	// ------------------------------------------------------------------
	// 지표 3 — 유형별 건수·비율
	// ------------------------------------------------------------------

	private List<DashboardResponse.CategoryCount> categoryCounts() {
		Map<FeedbackCategory, Long> counts = new LinkedHashMap<>();
		for (FeedbackCategory category : FeedbackCategory.values()) {
			counts.put(category, 0L);
		}
		for (FeedbackRepository.CategoryCount row : feedbackRepository.countByCategory()) {
			counts.put(row.getCategory(), row.getCount());
		}

		long total = counts.values().stream().mapToLong(Long::longValue).sum();

		List<DashboardResponse.CategoryCount> result = new ArrayList<>();
		for (FeedbackCategory category : FeedbackCategory.values()) {
			long count = counts.get(category);
			// 전체가 0건(시드도 등록도 없는 극단적인 경우)이면 0으로 나누는 대신 비율을 0으로 둔다.
			double ratio = total == 0 ? 0.0 : (double) count / total;
			result.add(new DashboardResponse.CategoryCount(category, count, ratio));
		}
		return result;
	}

	// ------------------------------------------------------------------
	// 지표 4 — 기간별 추이(최근 30일)
	// ------------------------------------------------------------------

	private List<DashboardResponse.DailyCount> dailyTrend() {
		LocalDate today = LocalDate.now();
		LocalDate start = today.minusDays(TREND_DAYS - 1L);

		// 날짜 -> 건수, 30일 전부 0으로 미리 채워서 데이터 없는 날이 그래프에서 안 비게 한다.
		Map<LocalDate, Long> counts = new LinkedHashMap<>();
		for (int i = 0; i < TREND_DAYS; i++) {
			counts.put(start.plusDays(i), 0L);
		}

		for (LocalDateTime createdAt : feedbackRepository.findCreatedAtFrom(start.atStartOfDay())) {
			LocalDate date = createdAt.toLocalDate();
			counts.computeIfPresent(date, (d, count) -> count + 1);
		}

		List<DashboardResponse.DailyCount> result = new ArrayList<>();
		counts.forEach((date, count) -> result.add(new DashboardResponse.DailyCount(date.toString(), count)));
		return result;
	}

	// ------------------------------------------------------------------
	// 지표 5 — 평균 처리 소요 시간 (DONE·REJECTED 만)
	// ------------------------------------------------------------------

	private double averageProcessingHours() {
		List<FeedbackRepository.ProcessingDuration> durations = feedbackRepository.findProcessingDurations(
				List.of(FeedbackStatus.DONE, FeedbackStatus.REJECTED));

		if (durations.isEmpty()) {
			return 0.0;
		}

		double totalHours = 0.0;
		for (FeedbackRepository.ProcessingDuration row : durations) {
			totalHours += Duration.between(row.getCreatedAt(), row.getClosedAt()).toMinutes() / 60.0;
		}
		return totalHours / durations.size();
	}

	// ------------------------------------------------------------------
	// 지표 6 — 이용자 수
	// ------------------------------------------------------------------

	private DashboardResponse.UserCount userCount() {
		long member = feedbackRepository.countDistinctAuthorsByAuthorType(AuthorType.MEMBER);
		long guest = feedbackRepository.countByAuthorType(AuthorType.GUEST);
		return new DashboardResponse.UserCount(member, guest);
	}

	// ------------------------------------------------------------------
	// 우선 처리 요청 목록 / 최근 접수 목록 — 둘 다 AdminFeedbackSummary 로 변환
	// ------------------------------------------------------------------

	private List<AdminFeedbackSummary> toSummaries(List<Feedback> feedbacks) {
		Map<Long, Boolean> answeredById = answeredMap(feedbacks);
		return feedbacks.stream()
				.map(feedback -> AdminFeedbackMapper.toSummary(feedback,
						answeredById.getOrDefault(feedback.getId(), false)))
				.toList();
	}

	/** AdminFeedbackController#answeredFeedbackIds 와 같은 이유로 여기도 따로 둔다(클래스 설명 참고). */
	private Map<Long, Boolean> answeredMap(List<Feedback> feedbacks) {
		List<Long> ids = feedbacks.stream().map(Feedback::getId).toList();
		if (ids.isEmpty()) {
			return Map.of();
		}
		Map<Long, Boolean> answered = new HashMap<>();
		for (Long answeredId : answerRepository.findFeedbackIdsWithAnswer(ids)) {
			answered.put(answeredId, true);
		}
		return answered;
	}
}
