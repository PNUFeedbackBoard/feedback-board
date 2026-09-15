package kr.ac.pusan.feedback.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.Answer;
import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.AnswerRepository;
import kr.ac.pusan.feedback.domain.repository.FeedbackRepository;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 개발용 시드 데이터. 기획안 13-5의 0-1단계 산출물.
 *
 * <p>dev 프로필에서만 동작한다. 애플리케이션이 기동될 때 프로젝트 5건, 계정 10건,
 * 피드백 40건, 답변 15건을 넣는다. 이미 데이터가 있으면 아무것도 하지 않는다.
 *
 * <p>화면을 만드는 사람이 이 데이터로 실제 레이아웃과 집계를 확인한다. 따라서 건수뿐 아니라
 * <b>분포</b>가 중요하다. 피드백 40건은 아래 분포를 만족하도록 {@link #FEEDBACK_SEEDS} 표에 고정해 두었다.
 *
 * <pre>
 *   프로젝트 : 5개 프로젝트에 각 8건
 *   상태     : RECEIVED 12 / IN_PROGRESS 6 / DONE 18 / REJECTED 4
 *   유형     : BUG 20 / FEATURE 13 / ETC 7
 *   중요도   : HIGH 8 / NORMAL 24 / LOW 8            (개발자 확정값 priority 기준)
 *   우선요청 : 3건 (모두 priority = HIGH)
 *   작성자   : GUEST 10건(author 가 null) / MEMBER 30건
 *   등록일   : 오늘 기준 1~29일 전에 분산 (일자별 추이 차트용)
 *   종료     : DONE·REJECTED 22건은 closedAt 이 createdAt 이후 (평균 처리 시간 집계용)
 *   답변     : DONE 이면서 회원이 쓴 15건. 작성자는 데모 개발자
 * </pre>
 *
 * <p>표를 고칠 때는 위 분포가 깨지지 않는지 함께 확인한다. 분포가 무너지면 대시보드 화면이
 * 비어 보이거나 한쪽으로 쏠려서 레이아웃 확인이 되지 않는다.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final FeedbackRepository feedbackRepository;
	private final AnswerRepository answerRepository;

	@Override
	public void run(ApplicationArguments args) {
		if (projectRepository.count() > 0 || userRepository.count() > 0 || feedbackRepository.count() > 0) {
			log.info("시드 데이터: 기존 데이터가 있어 건너뛴다.");
			return;
		}

		Map<String, Project> projects = seedProjects();
		Map<String, User> users = seedUsers();
		seedFeedbacks(projects, users);

		log.info("시드 데이터 생성 완료 - 프로젝트 {}건, 계정 {}건, 피드백 {}건, 답변 {}건",
				projectRepository.count(), userRepository.count(),
				feedbackRepository.count(), answerRepository.count());
	}

	// ------------------------------------------------------------------
	// 프로젝트
	// ------------------------------------------------------------------

	/**
	 * 기획안 1-2의 대상 사이트 5건을 넣는다. sortOrder 는 선언 순서대로 0부터 매긴다.
	 *
	 * @return code 를 키로 하는 저장된 프로젝트 맵
	 */
	private Map<String, Project> seedProjects() {
		Map<String, Project> byCode = new LinkedHashMap<>();
		int sortOrder = 0;
		for (ProjectSeed seed : PROJECT_SEEDS) {
			Project project = projectRepository.save(Project.builder()
					.code(seed.code())
					.name(seed.name())
					// TODO(D, 0-2단계): 로고 이미지가 준비되면 실제 주소로 채운다(기획안 12장 미결정 1).
					.logoUrl(null)
					// 자리표시자 주소다. 8단계 배포 시 각 시스템의 실제 주소로 바꾼다(기획안 12장 미결정 5).
					.siteUrl("https://example.pusan.ac.kr/" + seed.code())
					.sortOrder(sortOrder++)
					.build());
			byCode.put(project.getCode(), project);
		}
		return byCode;
	}

	// ------------------------------------------------------------------
	// 계정
	// ------------------------------------------------------------------

	/**
	 * 데모 계정 4건(기획안 13-2)과 일반 회원 6건을 넣는다.
	 *
	 * <p>googleSub 는 7단계 전까지 null 이다. createdAt 은 가입 순서가 드러나도록
	 * 60일 전부터 선언 순서대로 하루씩 뒤로 밀어 넣는다. 모든 계정이 가장 오래된 피드백(29일 전)보다
	 * 먼저 만들어지므로 계정 관리 화면의 가입일 정렬이 자연스럽게 보인다.
	 *
	 * @return email 을 키로 하는 저장된 계정 맵
	 */
	private Map<String, User> seedUsers() {
		Map<String, User> byEmail = new LinkedHashMap<>();
		LocalDateTime base = LocalDate.now().minusDays(60).atTime(10, 0);
		int index = 0;
		for (UserSeed seed : USER_SEEDS) {
			User user = userRepository.save(User.builder()
					.email(seed.email())
					.name(seed.name())
					.googleSub(null)
					.role(seed.role())
					.status(seed.status())
					.createdAt(base.plusDays(index++))
					.build());
			byEmail.put(user.getEmail(), user);
		}
		return byEmail;
	}

	// ------------------------------------------------------------------
	// 피드백과 답변
	// ------------------------------------------------------------------

	/**
	 * 피드백 40건과 답변 15건을 넣는다.
	 *
	 * <p>회원 피드백의 작성자는 {@link #MEMBER_AUTHOR_EMAILS} 순서대로 돌아가며 배정한다.
	 * 첫 번째가 데모 사용자이므로 user@demo.local 로 로그인하면 내 문의 목록에 여러 건이 보이고
	 * 그중 답변이 달린 건도 함께 보인다.
	 */
	private void seedFeedbacks(Map<String, Project> projects, Map<String, User> users) {
		List<User> memberAuthors = MEMBER_AUTHOR_EMAILS.stream().map(users::get).toList();
		User answerAuthor = users.get("dev@demo.local");
		LocalDate baseDate = LocalDate.now();
		int memberIndex = 0;

		for (int i = 0; i < FEEDBACK_SEEDS.size(); i++) {
			FeedbackSeed seed = FEEDBACK_SEEDS.get(i);

			boolean guest = seed.guest();
			User author = guest ? null : memberAuthors.get(memberIndex++ % memberAuthors.size());
			AuthorType authorType = guest ? AuthorType.GUEST : AuthorType.MEMBER;

			// 등록 시각은 업무 시간(09~17시) 안에서 흩어지도록 인덱스로 계산한다.
			LocalDateTime createdAt = baseDate.minusDays(seed.daysAgo())
					.atTime(9 + (i % 9), (i * 7) % 60);
			LocalDateTime closedAt = seed.closeAfterHours() == null
					? null
					: createdAt.plusHours(seed.closeAfterHours());
			// 답변은 종료보다 앞선 시각에 달린다. 답변이 없으면 firstAnsweredAt 도 null 이다.
			LocalDateTime firstAnsweredAt = (seed.answer() == null || seed.closeAfterHours() == null)
					? null
					: createdAt.plusHours(Math.max(1, seed.closeAfterHours() * 2 / 3));

			Feedback feedback = Feedback.seed(
					projects.get(seed.projectCode()), author, authorType,
					seed.title(), seed.content(), seed.category(), seed.reportedPriority(), createdAt,
					seed.status(), seed.priority(), seed.priorityRequested(), firstAnsweredAt, closedAt);
			Feedback saved = feedbackRepository.save(feedback);

			if (seed.answer() != null) {
				answerRepository.save(Answer.builder()
						.feedback(saved)
						.author(answerAuthor)
						.content(seed.answer())
						.createdAt(firstAnsweredAt)
						.updatedAt(firstAnsweredAt)
						.build());
			}
		}
	}

	// ------------------------------------------------------------------
	// 시드 표
	// ------------------------------------------------------------------

	/** 프로젝트 한 건. sortOrder 는 선언 순서로 정해지므로 필드에 두지 않는다. */
	private record ProjectSeed(String code, String name) {
	}

	/** 계정 한 건. */
	private record UserSeed(String email, String name, Role role, UserStatus status) {
	}

	/**
	 * 피드백 한 건.
	 *
	 * @param projectCode      대상 프로젝트 code
	 * @param reportedPriority 작성자가 고른 체감 긴급도. 정렬에 쓰지 않는다
	 * @param priority         개발자가 확정한 값. 정렬 기준이다
	 * @param guest            true 면 비회원 등록(author 는 null)
	 * @param daysAgo          오늘로부터 며칠 전에 등록되었는지
	 * @param closeAfterHours  등록 후 몇 시간 뒤에 종료되었는지. 종료되지 않았으면 null
	 * @param answer           달린 답변 내용. 없으면 null
	 */
	private record FeedbackSeed(String projectCode, String title, String content,
			FeedbackCategory category, FeedbackStatus status,
			Priority reportedPriority, Priority priority,
			boolean priorityRequested, boolean guest,
			int daysAgo, Integer closeAfterHours, String answer) {
	}

	/** 기획안 1-2의 대상 사이트. 선언 순서가 하단 디스크의 배치 순서(sortOrder 0~4)가 된다. */
	private static final List<ProjectSeed> PROJECT_SEEDS = List.of(
			new ProjectSeed("codeplace", "코드플레이스"),
			new ProjectSeed("aipms", "AIPMS"),
			new ProjectSeed("aicms", "AICMS"),
			new ProjectSeed("aicap", "AI역량지원시스템"),
			new ProjectSeed("srvadm", "pickle (서버관리)"));

	/** 데모 계정 4건(기획안 13-2)과 일반 회원 6건. */
	private static final List<UserSeed> USER_SEEDS = List.of(
			new UserSeed("dev@demo.local", "데모 개발자", Role.DEVELOPER, UserStatus.ACTIVE),
			new UserSeed("viewer@demo.local", "데모 열람자", Role.VIEWER, UserStatus.ACTIVE),
			new UserSeed("user@demo.local", "데모 사용자", Role.USER, UserStatus.ACTIVE),
			new UserSeed("pending@demo.local", "승인 대기", Role.USER, UserStatus.PENDING),
			new UserSeed("user1@example.com", "김민준", Role.USER, UserStatus.ACTIVE),
			new UserSeed("user2@example.com", "이서연", Role.USER, UserStatus.ACTIVE),
			new UserSeed("user3@example.com", "박지호", Role.USER, UserStatus.ACTIVE),
			new UserSeed("user4@example.com", "최수빈", Role.USER, UserStatus.ACTIVE),
			new UserSeed("user5@example.com", "정도현", Role.USER, UserStatus.ACTIVE),
			new UserSeed("user6@example.com", "한예린", Role.USER, UserStatus.ACTIVE));

	/**
	 * 회원 피드백의 작성자 후보. 순서대로 돌아가며 배정한다.
	 *
	 * <p>데모 사용자를 맨 앞에 둬서 user@demo.local 이 확실히 여러 건을 갖도록 한다.
	 * 승인 대기 계정(pending@demo.local)과 관리용 계정은 피드백을 쓰지 않는다.
	 */
	private static final List<String> MEMBER_AUTHOR_EMAILS = List.of(
			"user@demo.local",
			"user1@example.com",
			"user2@example.com",
			"user3@example.com",
			"user4@example.com",
			"user5@example.com",
			"user6@example.com");

	/**
	 * 피드백 40건. 프로젝트별로 8건씩 묶어 두었다.
	 *
	 * <p>클래스 주석의 분포표와 짝을 이룬다. 행을 고치면 분포가 바뀌므로 함께 확인한다.
	 */
	private static final List<FeedbackSeed> FEEDBACK_SEEDS = List.of(

			// ---- 코드플레이스 8건 (RECEIVED 3 / IN_PROGRESS 1 / DONE 3 / REJECTED 1) ----
			new FeedbackSeed("codeplace",
					"실습 채점이 '채점 중'에서 더 이상 진행되지 않습니다",
					"어제 오후부터 파이썬 3주차 실습을 제출하면 채점 상태가 '채점 중'에서 멈춘 채 30분이 지나도 바뀌지 않습니다. "
							+ "같은 분반 학생 두 명도 같은 증상이라고 합니다. 제출 기한이 내일까지라 확인 부탁드립니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.HIGH, Priority.HIGH, true, false, 2, null, null),
			new FeedbackSeed("codeplace",
					"에디터에 붙여넣은 한글 주석이 물음표로 깨집니다",
					"실습 코드에 한글 주석을 붙여넣고 저장한 뒤 다시 열면 주석이 전부 물음표로 표시됩니다. "
							+ "크롬과 엣지에서 모두 같은 증상입니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.HIGH, Priority.NORMAL, false, true, 5, null, null),
			new FeedbackSeed("codeplace",
					"제출 버튼을 누르면 직전에 작성한 코드가 제출됩니다",
					"코드를 수정한 뒤 바로 제출하면 수정 전 코드가 채점됩니다. 새로고침을 한 번 하고 제출하면 정상입니다. "
							+ "모르고 제출한 학생들이 점수를 잃고 있습니다.",
					FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS,
					Priority.HIGH, Priority.HIGH, false, false, 9, null, null),
			new FeedbackSeed("codeplace",
					"강의 영상이 특정 구간에서 멈추고 재생되지 않습니다",
					"2단원 강의 영상이 12분 지점에서 항상 멈춥니다. 재생 위치를 뒤로 옮겨도 그 지점부터는 소리만 나옵니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 17, 30,
					"영상 인코딩 파일 일부가 손상되어 생긴 문제였습니다. 해당 영상을 다시 올렸고 전 구간 재생을 확인했습니다. "
							+ "브라우저 캐시를 지운 뒤 다시 시청해 주세요."),
			new FeedbackSeed("codeplace",
					"제출 전에 코드를 임시 저장할 수 있으면 좋겠습니다",
					"실습 도중 브라우저를 실수로 닫으면 작성하던 코드가 모두 사라집니다. 자동 임시 저장이 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED,
					Priority.NORMAL, Priority.NORMAL, false, true, 12, null, null),
			new FeedbackSeed("codeplace",
					"채점 결과에 실패한 테스트 케이스의 입력값을 보여 주세요",
					"지금은 몇 개가 틀렸는지만 표시되어 어느 부분이 잘못됐는지 알기 어렵습니다. "
							+ "실패한 케이스의 입력값과 기대 출력값을 함께 보여 주시면 학습에 도움이 될 것 같습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 22, 96,
					"요청하신 대로 채점 결과 화면에 실패한 케이스의 입력값과 기대 출력값을 함께 표시하도록 반영했습니다. "
							+ "다만 채점 정책상 숨김 처리된 케이스는 입력값만 노출됩니다."),
			new FeedbackSeed("codeplace",
					"에디터 글꼴 크기를 조절하고 싶습니다",
					"에디터 글씨가 작아서 오래 보면 눈이 쉽게 피로합니다. 크기를 조절하는 버튼이 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.LOW, false, false, 27, 120,
					"에디터 오른쪽 위에 글꼴 크기 조절 버튼을 추가했습니다. 설정한 크기는 브라우저에 저장되어 "
							+ "다음 접속 때도 그대로 유지됩니다."),
			new FeedbackSeed("codeplace",
					"이번 주 실습 제출 기한을 일괄로 3일 연장해 주세요",
					"다른 과목 과제가 같은 주에 몰려 있습니다. 이번 주 실습 기한만 3일씩 미뤄 주셨으면 합니다.",
					FeedbackCategory.ETC, FeedbackStatus.REJECTED,
					Priority.HIGH, Priority.NORMAL, false, false, 25, 48, null),

			// ---- AIPMS 8건 (RECEIVED 2 / IN_PROGRESS 2 / DONE 4 / REJECTED 0) ----
			new FeedbackSeed("aipms",
					"산출물 파일이 10MB를 넘으면 업로드가 실패합니다",
					"중간 보고서 PDF를 올리면 '업로드에 실패했습니다'만 뜨고 더 진행되지 않습니다. 파일 크기는 14MB입니다. "
							+ "용량 제한이 있다면 화면에 안내해 주셨으면 합니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.HIGH, Priority.NORMAL, false, true, 3, null, null),
			new FeedbackSeed("aipms",
					"프로젝트 일정 간트 차트가 열리지 않습니다",
					"일정 탭에 들어가면 로딩 표시만 계속 돌고 차트가 나타나지 않습니다. 참여 인원이 스무 명 넘는 "
							+ "프로젝트에서만 이런 것 같습니다. 이번 주 점검 회의에 일정표가 필요합니다.",
					FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS,
					Priority.HIGH, Priority.HIGH, true, false, 6, null, null),
			new FeedbackSeed("aipms",
					"주간 보고 작성 화면에 지난주 내용이 그대로 남아 있습니다",
					"새 주차 보고를 열면 지난주에 쓴 내용이 채워진 상태로 열립니다. 지우지 않고 제출하면 "
							+ "같은 내용이 두 번 올라갑니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 14, 26,
					"직전 주차의 임시 저장 내용을 불러오는 조건이 잘못되어 있었습니다. 새 주차는 빈 양식으로 열리도록 "
							+ "수정했습니다."),
			new FeedbackSeed("aipms",
					"참여 인원을 추가해도 목록에 바로 보이지 않습니다",
					"인원을 추가하고 저장하면 성공 메시지는 나오는데 목록에는 나타나지 않습니다. 화면을 새로고침하면 보입니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 19, 52,
					"저장 후 목록을 다시 불러오지 않던 문제였습니다. 이제 저장하는 즉시 목록이 갱신됩니다."),
			new FeedbackSeed("aipms",
					"프로젝트 목록을 마감일 순으로 정렬하고 싶습니다",
					"지금은 등록일 순으로만 정렬됩니다. 마감이 임박한 프로젝트를 먼저 보고 싶습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED,
					Priority.LOW, Priority.NORMAL, false, false, 8, null, null),
			new FeedbackSeed("aipms",
					"산출물 목록을 엑셀 파일로 내려받게 해 주세요",
					"분기 보고를 만들 때 산출물 목록을 하나씩 옮겨 적고 있습니다. 목록을 파일로 내려받을 수 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.LOW, false, false, 24, 150,
					"산출물 목록 화면에 내려받기 버튼을 추가했습니다. 화면에 적용한 필터 기준으로 파일이 만들어집니다."),
			new FeedbackSeed("aipms",
					"산출물 제출 방식을 정리한 안내 문서가 있으면 좋겠습니다",
					"어떤 단계에 무엇을 제출해야 하는지 매번 물어봐야 합니다. 화면 안에 안내가 있으면 좋겠습니다.",
					FeedbackCategory.ETC, FeedbackStatus.IN_PROGRESS,
					Priority.LOW, Priority.NORMAL, false, true, 11, null, null),
			new FeedbackSeed("aipms",
					"프로젝트 단계 이름이 화면마다 다르게 표기됩니다",
					"목록에서는 '착수'인데 상세에서는 '시작'으로 나옵니다. 같은 단계인지 헷갈립니다.",
					FeedbackCategory.ETC, FeedbackStatus.DONE,
					Priority.LOW, Priority.LOW, false, false, 28, 72,
					"'착수 / 중간 / 최종'으로 용어를 통일하고 관련 화면 표기를 모두 맞췄습니다. 알려 주셔서 감사합니다."),

			// ---- AICMS 8건 (RECEIVED 3 / IN_PROGRESS 1 / DONE 3 / REJECTED 1) ----
			new FeedbackSeed("aicms",
					"등록한 공지 게시글이 목록에 보이지 않습니다",
					"어제 등록한 공지가 목록에 나타나지 않습니다. 상세 주소를 직접 입력하면 글은 열립니다. "
							+ "오늘 중으로 안내해야 하는 내용이라 급합니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.HIGH, Priority.HIGH, true, false, 1, null, null),
			new FeedbackSeed("aicms",
					"첨부한 이미지가 옆으로 돌아간 상태로 등록됩니다",
					"휴대폰으로 찍은 사진을 첨부하면 미리보기는 정상인데 등록 후에는 90도 돌아가 있습니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.NORMAL, Priority.NORMAL, false, true, 4, null, null),
			new FeedbackSeed("aicms",
					"본문 편집기에서 표를 넣으면 줄 간격이 무너집니다",
					"표를 넣은 뒤 아래에 글을 쓰면 줄 간격이 갑자기 넓어집니다. 저장하고 다시 열어도 그대로입니다.",
					FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS,
					Priority.NORMAL, Priority.NORMAL, false, false, 10, null, null),
			new FeedbackSeed("aicms",
					"두 글자로 검색하면 결과가 하나도 나오지 않습니다",
					"'수업'으로 검색하면 결과가 없다고 나오는데, '수업 자료'로 검색하면 글이 나옵니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.HIGH, Priority.NORMAL, false, true, 16, 40, null),
			new FeedbackSeed("aicms",
					"게시글 예약 발행 기능이 필요합니다",
					"공지를 정해진 시각에 맞춰 올리려고 새벽에 직접 등록하고 있습니다. 예약 발행이 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED,
					Priority.NORMAL, Priority.LOW, false, false, 13, null, null),
			new FeedbackSeed("aicms",
					"배너 이미지 순서를 화면에서 바꿀 수 있게 해 주세요",
					"배너 순서를 바꾸려면 지웠다가 다시 등록해야 합니다. 목록에서 순서만 바꿀 수 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 21, 64,
					"배너 관리 화면에서 끌어서 순서를 바꿀 수 있도록 반영했습니다. 바꾼 순서는 저장 버튼을 눌러야 적용됩니다."),
			new FeedbackSeed("aicms",
					"작성 중인 글이 사라지지 않도록 자동 저장을 넣어 주세요",
					"긴 글을 쓰다가 세션이 끊겨 두 번이나 처음부터 다시 썼습니다. 자동 저장이 꼭 필요합니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.HIGH, Priority.HIGH, false, false, 26, 110,
					"본문 편집기에 1분 간격 자동 저장을 추가했습니다. 브라우저가 닫히거나 세션이 끊겨도 마지막 자동 저장 "
							+ "내용을 불러올 수 있습니다."),
			new FeedbackSeed("aicms",
					"관리자 화면 색상을 학교 상징색으로 바꿔 주세요",
					"다른 교내 시스템과 색이 달라서 어색합니다. 상징색으로 맞춰 주시면 좋겠습니다.",
					FeedbackCategory.ETC, FeedbackStatus.REJECTED,
					Priority.LOW, Priority.LOW, false, false, 23, 36, null),

			// ---- AI역량지원시스템 8건 (RECEIVED 2 / IN_PROGRESS 1 / DONE 4 / REJECTED 1) ----
			new FeedbackSeed("aicap",
					"역량 진단 결과 그래프가 휴대폰에서 잘려 보입니다",
					"휴대폰으로 결과를 열면 그래프 오른쪽이 화면 밖으로 나가서 항목 이름이 보이지 않습니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.NORMAL, Priority.NORMAL, false, true, 2, null, null),
			new FeedbackSeed("aicap",
					"이수한 교육이 마일리지에 반영되지 않았습니다",
					"지난달에 수료한 과정 두 건이 마일리지 합계에 들어가 있지 않습니다. 수료증은 정상 발급되었습니다. "
							+ "장학 신청 기준에 들어가는 점수라 확인이 필요합니다.",
					FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS,
					Priority.HIGH, Priority.HIGH, false, false, 7, null, null),
			new FeedbackSeed("aicap",
					"수료증 PDF에서 이름이 깨져서 출력됩니다",
					"수료증을 내려받으면 이름 부분만 네모 기호로 나옵니다. 화면 미리보기에서는 정상입니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.HIGH, Priority.NORMAL, false, false, 15, 20,
					"수료증 서식에 한글 글꼴이 포함되어 있지 않아 생긴 문제였습니다. 글꼴을 포함하도록 수정했으니 "
							+ "다시 내려받아 확인해 주세요."),
			new FeedbackSeed("aicap",
					"진단 설문을 끝까지 제출했는데 미완료로 표시됩니다",
					"마지막 문항까지 답하고 제출 버튼을 눌렀는데 목록에서는 '미완료'로 나옵니다. 다시 들어가면 "
							+ "답변은 그대로 남아 있습니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.HIGH, Priority.NORMAL, false, false, 20, 45,
					"마지막 문항 저장과 완료 처리가 동시에 일어나면 완료 표시가 누락되는 문제였습니다. 처리 순서를 "
							+ "바로잡았고 같은 증상이던 기존 기록도 함께 정정했습니다."),
			new FeedbackSeed("aicap",
					"관심 분야를 고르면 맞는 교육을 추천해 주면 좋겠습니다",
					"교육이 너무 많아서 무엇을 들어야 할지 고르기 어렵습니다. 진단 결과에 맞춰 추천해 주면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.RECEIVED,
					Priority.LOW, Priority.LOW, false, false, 9, null, null),
			new FeedbackSeed("aicap",
					"역량 진단 결과를 이전 회차와 비교해서 보고 싶습니다",
					"이번 회차 결과만 보여서 얼마나 나아졌는지 알 수 없습니다. 지난 회차와 나란히 보여 주세요.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, true, 18, 80, null),
			new FeedbackSeed("aicap",
					"마일리지 산정 기준을 화면에서 안내해 주세요",
					"어떤 교육이 몇 점인지 알 수 없어 계획을 세우기 어렵습니다. 기준표를 화면에 올려 주셨으면 합니다.",
					FeedbackCategory.ETC, FeedbackStatus.DONE,
					Priority.LOW, Priority.NORMAL, false, false, 25, 130,
					"마일리지 화면 위쪽에 산정 기준 안내를 추가했습니다. 과정 유형별 배점표도 함께 확인하실 수 있습니다."),
			new FeedbackSeed("aicap",
					"다른 학교에서 들은 이수 내역도 등록할 수 있게 해 주세요",
					"교류 학점으로 들은 교육이 있는데 등록할 방법이 없습니다.",
					FeedbackCategory.ETC, FeedbackStatus.REJECTED,
					Priority.NORMAL, Priority.LOW, false, false, 29, 60, null),

			// ---- pickle (서버관리) 8건 (RECEIVED 2 / IN_PROGRESS 1 / DONE 4 / REJECTED 1) ----
			new FeedbackSeed("srvadm",
					"실습 서버 접속이 오후부터 계속 끊깁니다",
					"오후 2시쯤부터 SSH 접속이 몇 분 간격으로 끊어집니다. 같은 연구실 인원 모두 같은 증상이라 "
							+ "학습 작업을 이어서 돌리지 못하고 있습니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.HIGH, Priority.HIGH, false, false, 1, null, null),
			new FeedbackSeed("srvadm",
					"자원 사용량 그래프가 어제 이후로 갱신되지 않습니다",
					"대시보드의 CPU와 메모리 그래프가 어제 오후 시점에 멈춰 있습니다. 새로고침해도 같습니다.",
					FeedbackCategory.BUG, FeedbackStatus.RECEIVED,
					Priority.NORMAL, Priority.NORMAL, false, true, 5, null, null),
			new FeedbackSeed("srvadm",
					"GPU 할당 요청이 승인된 뒤에도 대기 상태로 표시됩니다",
					"승인 메일은 받았는데 요청 목록에서는 계속 '대기'로 보입니다. 실제로는 서버에 접속이 됩니다.",
					FeedbackCategory.BUG, FeedbackStatus.IN_PROGRESS,
					Priority.HIGH, Priority.NORMAL, false, false, 12, null, null),
			new FeedbackSeed("srvadm",
					"백업 완료 알림이 같은 내용으로 여러 번 옵니다",
					"매일 새벽에 백업 완료 메일이 서너 통씩 옵니다. 내용은 모두 같습니다.",
					FeedbackCategory.BUG, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 19, 28,
					"알림 발송 작업이 중복 등록되어 있었습니다. 중복 등록을 지웠고 이틀간 확인한 결과 하루 한 통만 "
							+ "발송되고 있습니다."),
			new FeedbackSeed("srvadm",
					"계정별 디스크 사용량을 목록에서 바로 보고 싶습니다",
					"용량이 찼을 때 누가 많이 쓰는지 확인하려면 서버에 직접 들어가야 합니다. 목록에 표시해 주세요.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.NORMAL, false, false, 22, 90,
					"계정 목록에 디스크 사용량 열을 추가했습니다. 값은 매일 새벽 집계 기준이며 열 제목을 눌러 "
							+ "정렬할 수 있습니다."),
			new FeedbackSeed("srvadm",
					"서버 점검 일정을 미리 공지할 수 있는 화면이 필요합니다",
					"점검 때문에 작업이 중단된 적이 여러 번 있습니다. 일정을 미리 알 수 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.NORMAL, Priority.HIGH, false, false, 27, 140,
					"점검 일정 등록 화면을 추가했습니다. 일정을 등록하면 대상 서버 이용자에게 메일이 발송되고 "
							+ "접속 화면에도 안내가 표시됩니다."),
			new FeedbackSeed("srvadm",
					"웹 콘솔에서 로그를 검색할 수 있으면 좋겠습니다",
					"로그를 보려면 파일을 통째로 내려받아야 합니다. 날짜와 단어로 검색할 수 있으면 좋겠습니다.",
					FeedbackCategory.FEATURE, FeedbackStatus.DONE,
					Priority.LOW, Priority.NORMAL, false, true, 16, 55, null),
			new FeedbackSeed("srvadm",
					"개인 계정으로 외부 접속을 열어 주세요",
					"집에서도 작업할 수 있게 외부에서 바로 접속할 수 있도록 해 주셨으면 합니다.",
					FeedbackCategory.ETC, FeedbackStatus.REJECTED,
					Priority.NORMAL, Priority.LOW, false, false, 24, 44, null));
}
