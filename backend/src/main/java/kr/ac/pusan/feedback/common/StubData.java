package kr.ac.pusan.feedback.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.ac.pusan.feedback.admin.dto.AdminFeedbackDetail;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackPage;
import kr.ac.pusan.feedback.admin.dto.AdminFeedbackSummary;
import kr.ac.pusan.feedback.admin.dto.AdminUserResponse;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse;
import kr.ac.pusan.feedback.admin.dto.PriorityRequestResponse;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.feedback.dto.AnswerResponse;
import kr.ac.pusan.feedback.feedback.dto.FeedbackCreateResponse;
import kr.ac.pusan.feedback.feedback.dto.MeResponse;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackDetail;
import kr.ac.pusan.feedback.feedback.dto.MyFeedbackSummary;
import kr.ac.pusan.feedback.feedback.dto.ProjectResponse;

/**
 * 0-1단계 스텁 응답 데이터. 기획안 13-5의 "API 명세와 스텁 응답" 산출물이다.
 *
 * <p>C 와 D 가 백엔드 완성을 기다리지 않고 화면을 끝까지 만들 수 있도록 하는 것이 목적이다(기획안 13-4).
 * DB 나 리포지토리를 쓰지 않고 고정값만 돌려준다.
 *
 * <p>규칙 세 가지.
 * <ul>
 *   <li>모든 시각은 LocalDateTime.of 로 만든 고정값이다. LocalDateTime.now() 를 쓰지 않는다.
 *       호출할 때마다 값이 바뀌면 화면에서 비교·확인이 어렵다.</li>
 *   <li>목록은 최신순으로 정렬해 둔다. 우선 처리 요청 항목의 상단 고정은 화면에서 확인한다.</li>
 *   <li>authorType 이 GUEST 인 항목은 authorName 이 null 이고 answered 가 false 다.
 *       비회원은 답변 대상이 아니다(기획안 6-4).</li>
 * </ul>
 *
 * <p>목록 14건에는 상태 4종, 유형 3종, 중요도 3종이 모두 들어 있고
 * 우선 처리 요청 2건과 비회원 3건이 포함된다. 칸반 3열, 반영 불가 목록, 비회원 비활성 표시를
 * 이 데이터만으로 전부 확인할 수 있다.
 *
 * <p>TODO(A/B, 1단계 이후): 각 담당자가 자기 API 를 실제 구현으로 바꾸면서
 * 이 클래스에서 쓰지 않게 된 메서드를 지운다. 전부 지워지면 클래스 자체를 삭제한다.
 */
public final class StubData {

	private StubData() {
	}

	// ------------------------------------------------------------------
	// 공개 · 사용자용
	// ------------------------------------------------------------------

	/**
	 * 프로젝트 5건. 기획안 1-2. logoUrl 은 이미지가 미결정이라 null 이다(기획안 12장 1번).
	 *
	 * <p>sortOrder(0~4)와 siteUrl 자리표시자는 DataSeeder 의 PROJECT_SEEDS 와 같은 값으로 맞춰 두었다.
	 * 2단계에서 이 스텁을 실제 조회로 바꿀 때 화면에 보이는 값이 달라지지 않게 하기 위한 것이다.
	 */
	public static List<ProjectResponse> projects() {
		return List.of(
				new ProjectResponse(1L, "codeplace", "코드플레이스", null, "https://example.pusan.ac.kr/codeplace", 0),
				new ProjectResponse(2L, "aipms", "AIPMS", null, "https://example.pusan.ac.kr/aipms", 1),
				new ProjectResponse(3L, "aicms", "AICMS", null, "https://example.pusan.ac.kr/aicms", 2),
				new ProjectResponse(4L, "aicap", "AI역량지원시스템", null, "https://example.pusan.ac.kr/aicap", 3),
				new ProjectResponse(5L, "srvadm", "pickle (서버관리)", null, "https://example.pusan.ac.kr/srvadm", 4));
	}

	/** 로그인한 계정. 데모 계정 user@demo.local 을 고정으로 돌려준다(기획안 13-2) */
	public static MeResponse me() {
		return new MeResponse(3L, "user@demo.local", "데모 사용자", Role.USER, UserStatus.ACTIVE);
	}

	/** 피드백 등록 결과 */
	public static FeedbackCreateResponse feedbackCreated() {
		return new FeedbackCreateResponse(1001L);
	}

	/** 내 문의 목록. user@demo.local 이 등록한 4건이라고 가정한다 */
	public static List<MyFeedbackSummary> myFeedbacks() {
		return List.of(
				new MyFeedbackSummary(201L, "codeplace", "코드플레이스",
						"로그인 후 첫 화면이 빈 화면으로 표시됩니다",
						FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
						LocalDateTime.of(2026, 9, 14, 9, 12), false),
				new MyFeedbackSummary(202L, "aipms", "AIPMS",
						"제출물 일괄 다운로드 기능이 필요합니다",
						FeedbackCategory.FEATURE, FeedbackStatus.IN_PROGRESS,
						LocalDateTime.of(2026, 9, 9, 16, 33), false),
				new MyFeedbackSummary(203L, "aicms", "AICMS",
						"출석 통계가 실제와 다르게 집계됩니다",
						FeedbackCategory.BUG, FeedbackStatus.REJECTED,
						LocalDateTime.of(2026, 9, 7, 13, 47), true),
				new MyFeedbackSummary(204L, "aicap", "AI역량지원시스템",
						"진단 결과 PDF 저장이 실패합니다",
						FeedbackCategory.BUG, FeedbackStatus.DONE,
						LocalDateTime.of(2026, 9, 4, 10, 10), true));
	}

	/**
	 * 내 문의 상세.
	 *
	 * <p>목록에 없는 id 로 호출하면 첫 번째 항목을 돌려준다. 스텁이므로 404 를 만들지 않는다.
	 * 화면이 어떤 id 로 들어와도 상세 화면을 그릴 수 있게 하기 위한 처리다.
	 */
	public static MyFeedbackDetail myFeedbackDetail(Long id) {
		MyFeedbackSummary summary = findMyFeedback(id);
		MyFeedbackDetail.AnswerView answer = summary.answered()
				? new MyFeedbackDetail.AnswerView(
						ANSWER_CONTENT,
						summary.createdAt().plusHours(30),
						summary.createdAt().plusHours(30))
				: null;

		return new MyFeedbackDetail(
				summary.id(),
				summary.projectCode(),
				summary.projectName(),
				summary.title(),
				contentOf(summary.title()),
				summary.category(),
				summary.status(),
				reportedPriorityOf(summary.id()),
				summary.createdAt(),
				answer);
	}

	// ------------------------------------------------------------------
	// 관리용
	// ------------------------------------------------------------------

	/**
	 * 관리용 목록 14건.
	 *
	 * <p>등록일 내림차순으로 정렬해 두었다.
	 * 상태: RECEIVED 4, IN_PROGRESS 4, DONE 4, REJECTED 2
	 * 유형: BUG 7, FEATURE 4, ETC 3
	 * 중요도(priority): HIGH 5, NORMAL 5, LOW 4
	 * 우선 처리 요청 2건(101, 109), 비회원 3건(103, 106, 111)
	 */
	public static List<AdminFeedbackSummary> adminFeedbacks() {
		return List.of(
				member(101L, "codeplace", "코드플레이스", "로그인 후 첫 화면이 빈 화면으로 표시됩니다",
						FeedbackCategory.BUG, FeedbackStatus.RECEIVED, Priority.HIGH, Priority.HIGH, true,
						"김민준", LocalDateTime.of(2026, 9, 14, 9, 12), false),
				member(102L, "codeplace", "코드플레이스", "코드 실행 결과 창이 아래쪽에서 잘려 보입니다",
						FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS, Priority.HIGH, Priority.NORMAL, false,
						"이서연", LocalDateTime.of(2026, 9, 13, 14, 5), false),
				guest(103L, "codeplace", "코드플레이스", "야간 작업을 위해 다크 모드를 추가해 주세요",
						FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED, Priority.NORMAL, Priority.LOW, false,
						LocalDateTime.of(2026, 9, 13, 10, 41)),
				member(104L, "aipms", "AIPMS", "제출 기한이 지나도 제출 버튼이 활성화됩니다",
						FeedbackCategory.BUG, FeedbackStatus.DONE, Priority.HIGH, Priority.HIGH, false,
						"박도윤", LocalDateTime.of(2026, 9, 10, 11, 20), true),
				member(105L, "aipms", "AIPMS", "제출물 일괄 다운로드 기능이 필요합니다",
						FeedbackCategory.FEATURE, FeedbackStatus.IN_PROGRESS, Priority.NORMAL, Priority.NORMAL, false,
						"최지우", LocalDateTime.of(2026, 9, 9, 16, 33), false),
				guest(106L, "aipms", "AIPMS", "안내 문구에 오타가 있습니다",
						FeedbackCategory.ETC, FeedbackStatus.DONE, Priority.LOW, Priority.LOW, false,
						LocalDateTime.of(2026, 9, 8, 9, 2)),
				member(107L, "aicms", "AICMS", "출석 통계가 실제와 다르게 집계됩니다",
						FeedbackCategory.BUG, FeedbackStatus.REJECTED, Priority.HIGH, Priority.NORMAL, false,
						"정하윤", LocalDateTime.of(2026, 9, 7, 13, 47), true),
				member(108L, "aicms", "AICMS", "게시판 검색 조건을 늘려 주세요",
						FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED, Priority.LOW, Priority.LOW, false,
						"강서준", LocalDateTime.of(2026, 9, 6, 8, 55), false),
				member(109L, "aicms", "AICMS", "휴대폰에서 표가 가로로 넘쳐 보입니다",
						FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS, Priority.NORMAL, Priority.HIGH, true,
						"윤채원", LocalDateTime.of(2026, 9, 5, 17, 21), false),
				member(110L, "aicap", "AI역량지원시스템", "진단 결과 PDF 저장이 실패합니다",
						FeedbackCategory.BUG, FeedbackStatus.DONE, Priority.HIGH, Priority.HIGH, false,
						"임나윤", LocalDateTime.of(2026, 9, 4, 10, 10), true),
				guest(111L, "aicap", "AI역량지원시스템", "역량 그래프에 비교 기준을 표시해 주세요",
						FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED, Priority.NORMAL, Priority.NORMAL, false,
						LocalDateTime.of(2026, 9, 3, 15, 38)),
				member(112L, "aicap", "AI역량지원시스템", "이용 안내가 어디에 있는지 찾기 어렵습니다",
						FeedbackCategory.ETC, FeedbackStatus.REJECTED, Priority.LOW, Priority.LOW, false,
						"오시현", LocalDateTime.of(2026, 9, 2, 9, 44), true),
				member(113L, "srvadm", "pickle (서버관리)", "서버 상태 알림이 중복으로 발송됩니다",
						FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS, Priority.HIGH, Priority.HIGH, false,
						"한지호", LocalDateTime.of(2026, 9, 1, 19, 2), false),
				member(114L, "srvadm", "pickle (서버관리)", "로그 보존 기간을 늘려 주세요",
						FeedbackCategory.ETC, FeedbackStatus.DONE, Priority.NORMAL, Priority.NORMAL, false,
						"서예린", LocalDateTime.of(2026, 8, 29, 11, 15), true));
	}

	/** 관리용 목록 응답. 스텁은 필터·정렬·페이지를 적용하지 않고 14건을 그대로 돌려준다 */
	public static AdminFeedbackPage adminFeedbackPage() {
		List<AdminFeedbackSummary> items = adminFeedbacks();
		return new AdminFeedbackPage(items, items.size());
	}

	/**
	 * 관리용 상세.
	 *
	 * <p>목록에 없는 id 로 호출하면 첫 번째 항목을 돌려준다. 스텁이므로 404 를 만들지 않는다.
	 * firstAnsweredAt 은 답변이 있는 경우에만, closedAt 은 DONE·REJECTED 인 경우에만 채운다.
	 */
	public static AdminFeedbackDetail adminFeedbackDetail(Long id) {
		AdminFeedbackSummary summary = findAdminFeedback(id);
		boolean closed = summary.status() == FeedbackStatus.DONE || summary.status() == FeedbackStatus.REJECTED;
		LocalDateTime firstAnsweredAt = summary.answered() ? summary.createdAt().plusHours(30) : null;
		LocalDateTime closedAt = closed ? summary.createdAt().plusHours(42) : null;
		AnswerResponse answer = summary.answered()
				? new AnswerResponse(900L + summary.id(), ANSWER_CONTENT, firstAnsweredAt, firstAnsweredAt)
				: null;

		return new AdminFeedbackDetail(
				summary.id(),
				summary.projectCode(),
				summary.projectName(),
				summary.title(),
				summary.category(),
				summary.status(),
				summary.reportedPriority(),
				summary.priority(),
				summary.priorityRequested(),
				summary.authorType(),
				summary.authorName(),
				summary.createdAt(),
				summary.answered(),
				contentOf(summary.title()),
				firstAnsweredAt,
				closedAt,
				answer);
	}

	/** 답변 등록·수정 결과. 스텁은 요청 본문을 읽지 않고 고정 답변을 돌려준다 */
	public static AnswerResponse answer(Long feedbackId) {
		LocalDateTime createdAt = LocalDateTime.of(2026, 9, 11, 17, 20);
		return new AnswerResponse(900L + feedbackId, ANSWER_CONTENT, createdAt, createdAt);
	}

	/** 우선 처리 요청 결과. 요청하면 priority 가 HIGH 로 올라간다(기획안 4-4) */
	public static PriorityRequestResponse priorityRequest(Long feedbackId) {
		return new PriorityRequestResponse(feedbackId, true, Priority.HIGH);
	}

	/** 대시보드 지표. 시드 데이터 40건을 가정한 값이다 */
	public static DashboardResponse dashboard() {
		List<AdminFeedbackSummary> all = adminFeedbacks();
		List<AdminFeedbackSummary> requested = all.stream()
				.filter(AdminFeedbackSummary::priorityRequested)
				.toList();
		List<AdminFeedbackSummary> recent = List.copyOf(all.subList(0, Math.min(10, all.size())));

		return new DashboardResponse(
				statusCounts(14, 9, 12, 5),
				List.of(
						new DashboardResponse.ProjectStatusCount("codeplace", "코드플레이스", statusCounts(4, 2, 3, 1)),
						new DashboardResponse.ProjectStatusCount("aipms", "AIPMS", statusCounts(3, 2, 3, 1)),
						new DashboardResponse.ProjectStatusCount("aicms", "AICMS", statusCounts(3, 2, 2, 1)),
						new DashboardResponse.ProjectStatusCount("aicap", "AI역량지원시스템", statusCounts(2, 2, 2, 1)),
						new DashboardResponse.ProjectStatusCount("srvadm", "pickle (서버관리)", statusCounts(2, 1, 2, 1))),
				List.of(
						new DashboardResponse.CategoryCount(FeedbackCategory.BUG, 22, 0.55),
						new DashboardResponse.CategoryCount(FeedbackCategory.FEATURE, 12, 0.30),
						new DashboardResponse.CategoryCount(FeedbackCategory.ETC, 6, 0.15)),
				dailyTrend(),
				36.5,
				new DashboardResponse.UserCount(18, 7),
				requested,
				recent);
	}

	/** 계정 목록. 데모 계정 4종이다(기획안 13-2) */
	public static List<AdminUserResponse> users() {
		return List.of(
				new AdminUserResponse(1L, "dev@demo.local", "데모 개발자",
						Role.DEVELOPER, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 1, 9, 0)),
				new AdminUserResponse(2L, "viewer@demo.local", "데모 열람자",
						Role.VIEWER, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 1, 9, 5)),
				new AdminUserResponse(3L, "user@demo.local", "데모 사용자",
						Role.USER, UserStatus.ACTIVE, LocalDateTime.of(2026, 9, 1, 9, 10)),
				new AdminUserResponse(4L, "pending@demo.local", "승인 대기",
						Role.USER, UserStatus.PENDING, LocalDateTime.of(2026, 9, 12, 13, 20)));
	}

	/** 계정 1건. 목록에 없는 id 로 호출하면 첫 번째 항목을 돌려준다 */
	public static AdminUserResponse user(Long id) {
		return users().stream()
				.filter(it -> it.id().equals(id))
				.findFirst()
				.orElse(users().get(0));
	}

	// ------------------------------------------------------------------
	// 내부 helper
	// ------------------------------------------------------------------

	private static final String ANSWER_CONTENT =
			"확인 후 수정하여 반영했습니다. 다시 확인해 보시고 같은 증상이 계속되면 알려 주세요.";

	/** 회원이 등록한 항목 */
	private static AdminFeedbackSummary member(Long id, String projectCode, String projectName, String title,
			FeedbackCategory category, FeedbackStatus status, Priority reportedPriority, Priority priority,
			boolean priorityRequested, String authorName, LocalDateTime createdAt, boolean answered) {
		return new AdminFeedbackSummary(id, projectCode, projectName, title, category, status,
				reportedPriority, priority, priorityRequested,
				AuthorType.MEMBER, authorName, createdAt, answered);
	}

	/** 비회원이 등록한 항목. authorName 은 null 이고 답변 대상이 아니므로 answered 는 항상 false 다 */
	private static AdminFeedbackSummary guest(Long id, String projectCode, String projectName, String title,
			FeedbackCategory category, FeedbackStatus status, Priority reportedPriority, Priority priority,
			boolean priorityRequested, LocalDateTime createdAt) {
		return new AdminFeedbackSummary(id, projectCode, projectName, title, category, status,
				reportedPriority, priority, priorityRequested,
				AuthorType.GUEST, null, createdAt, false);
	}

	private static AdminFeedbackSummary findAdminFeedback(Long id) {
		List<AdminFeedbackSummary> all = adminFeedbacks();
		return all.stream()
				.filter(it -> it.id().equals(id))
				.findFirst()
				.orElse(all.get(0));
	}

	private static MyFeedbackSummary findMyFeedback(Long id) {
		List<MyFeedbackSummary> all = myFeedbacks();
		return all.stream()
				.filter(it -> it.id().equals(id))
				.findFirst()
				.orElse(all.get(0));
	}

	/** 내 문의 상세에 쓸 작성자 선택 긴급도 */
	private static Priority reportedPriorityOf(Long id) {
		if (id == null) {
			return Priority.NORMAL;
		}
		return switch (id.intValue()) {
			case 202 -> Priority.NORMAL;
			case 203 -> Priority.LOW;
			default -> Priority.HIGH;
		};
	}

	/** 제목을 바탕으로 만든 본문. 화면의 줄바꿈·길이 확인용으로 두 문단을 둔다 */
	private static String contentOf(String title) {
		return title + "\n\n"
				+ "어제 오후부터 같은 증상이 반복해서 나타납니다. 다른 기기에서도 동일하게 재현되었습니다.\n"
				+ "확인 부탁드립니다.";
	}

	/** 네 가지 상태를 항상 모두 담은 건수 맵을 만든다 */
	private static Map<FeedbackStatus, Long> statusCounts(long received, long inProgress, long done, long rejected) {
		Map<FeedbackStatus, Long> counts = new LinkedHashMap<>();
		counts.put(FeedbackStatus.RECEIVED, received);
		counts.put(FeedbackStatus.IN_PROGRESS, inProgress);
		counts.put(FeedbackStatus.DONE, done);
		counts.put(FeedbackStatus.REJECTED, rejected);
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * 2026-08-17 부터 30일치 접수 건수. 날짜 문자열은 고정 기준일에서 계산한 값이다.
	 *
	 * <p>합이 40 이다. statusCounts·projectStatusCounts·categoryCounts 의 합과 일부러 맞춰 두었다.
	 * 이 값이 어긋나면 화면에서 "전체 40건" 옆의 추이 그래프만 총합이 다르게 보여
	 * C 가 집계 버그로 오해하게 된다. 숫자를 고칠 때는 합을 40 으로 유지한다.
	 */
	private static List<DashboardResponse.DailyCount> dailyTrend() {
		int[] counts = {1, 2, 0, 1, 3, 2, 0, 1, 2, 1, 0, 2, 3, 1, 1, 0, 2, 1, 2, 3, 0, 1, 2, 1, 2, 0, 3, 2, 1, 0};
		LocalDate start = LocalDate.of(2026, 8, 17);

		List<DashboardResponse.DailyCount> trend = new ArrayList<>(counts.length);
		for (int i = 0; i < counts.length; i++) {
			trend.add(new DashboardResponse.DailyCount(start.plusDays(i).toString(), counts[i]));
		}
		return List.copyOf(trend);
	}
}
