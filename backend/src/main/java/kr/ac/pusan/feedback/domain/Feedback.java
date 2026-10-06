package kr.ac.pusan.feedback.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.common.enums.Priority;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드백 1건. 기획안 4장, 8장.
 *
 * <p>생성은 {@link #create} 정적 팩터리로만 한다. 상태·유형·중요도를 바꾸는 도메인 메서드는
 * 0-1단계에서 만들지 않는다. 1단계 이후 담당자가 자기 기능과 함께 추가한다.
 *
 * <p>작성자가 등록한 피드백은 수정·삭제할 수 없다(기획안 5-3).
 */
@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_feedbacks_project"))
	private Project project;

	/** 비회원이 등록한 경우 null 이다. authorType 이 GUEST 인 행과 짝을 이룬다 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id",
			foreignKey = @ForeignKey(name = "fk_feedbacks_author"))
	private User author;

	/** 우선 처리 요청을 남긴 열람자. 기존 데이터와 요청이 없는 행은 null 이다 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "priority_requester_id",
			foreignKey = @ForeignKey(name = "fk_feedbacks_priority_requester"))
	private User priorityRequester;

	@Enumerated(EnumType.STRING)
	@Column(name = "author_type", nullable = false, length = 20)
	private AuthorType authorType;

	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@Column(name = "content", nullable = false, length = 2000, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, length = 20)
	private FeedbackCategory category;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private FeedbackStatus status;

	/** 작성자가 고른 체감 긴급도. 참고용이며 정렬에 쓰지 않는다 */
	@Enumerated(EnumType.STRING)
	@Column(name = "reported_priority", nullable = false, length = 20)
	private Priority reportedPriority;

	/** 개발자가 확정한 값. 정렬 기준이다 */
	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 20)
	private Priority priority;

	/** 열람자의 우선 처리 요청 여부. 정렬 1순위 기준 */
	@Column(name = "priority_requested", nullable = false)
	private boolean priorityRequested;

	/** 처리 담당자 표시 이름. 조직 계정 밖 담당자도 입력할 수 있어 문자열로 보관한다. */
	@Column(name = "assignee_name", length = 100)
	private String assigneeName;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	/** 첫 답변이 등록된 시각. 대시보드 지표에 쓴다 */
	@Column(name = "first_answered_at")
	private LocalDateTime firstAnsweredAt;

	/** DONE 또는 REJECTED 로 종료된 시각. 평균 처리 소요 시간 산출에 쓴다 */
	@Column(name = "closed_at")
	private LocalDateTime closedAt;

	private Feedback(Project project, User author, AuthorType authorType, String title, String content,
			FeedbackCategory category, Priority reportedPriority, LocalDateTime now) {
		this.project = project;
		this.author = author;
		this.authorType = authorType;
		this.title = title;
		this.content = content;
		this.category = category;
		this.status = FeedbackStatus.RECEIVED;
		this.reportedPriority = reportedPriority;
		this.priority = reportedPriority;
		this.priorityRequested = false;
		this.createdAt = now;
	}

	/**
	 * 등록용 정적 팩터리.
	 *
	 * <p>status 는 RECEIVED, priority 는 reportedPriority 와 같은 값, priorityRequested 는 false 로 시작한다.
	 * firstAnsweredAt 과 closedAt 은 null 이다.
	 *
	 * @param author 비회원이면 null 을 넘긴다
	 * @param now    등록 시각. 호출자가 주입한다
	 */
	public static Feedback create(Project project, User author, AuthorType authorType, String title, String content,
			FeedbackCategory category, Priority reportedPriority, LocalDateTime now) {
		return new Feedback(project, author, authorType, title, content, category, reportedPriority, now);
	}

	/**
	 * 개발용 시드 전용 팩터리. 등록 이후 처리까지 끝난 상태를 한 번에 만든다.
	 *
	 * <p><b>일반 코드에서 쓰지 마라.</b> 사용자가 등록하는 경로는 {@link #create} 하나뿐이고,
	 * 그 뒤의 상태·중요도 변경은 1단계에서 추가될 도메인 메서드로만 이뤄져야 한다.
	 * 이 팩터리는 {@code DataSeeder} 가 처리 중·처리 완료처럼 중간 상태인 표본 데이터를
	 * 만들기 위해서만 존재하며, dev 프로필 밖에서는 호출되지 않는다.
	 *
	 * <p>시드가 과거 시점과 처리 상태를 재현해야 하므로 일반 변경 메서드와 분리한다.
	 */
	public static Feedback seed(Project project, User author, AuthorType authorType, String title, String content,
			FeedbackCategory category, Priority reportedPriority, LocalDateTime createdAt,
			FeedbackStatus status, Priority priority, boolean priorityRequested,
			LocalDateTime firstAnsweredAt, LocalDateTime closedAt) {
		Feedback feedback = new Feedback(project, author, authorType, title, content,
				category, reportedPriority, createdAt);
		feedback.status = status;
		feedback.priority = priority;
		feedback.priorityRequested = priorityRequested;
		feedback.firstAnsweredAt = firstAnsweredAt;
		feedback.closedAt = closedAt;
		return feedback;
	}

	/** 관리자가 보낸 필드만 변경하고 종료 시각을 상태와 일관되게 유지한다. */
	public void update(FeedbackStatus status, FeedbackCategory category, Priority priority,
			String assigneeName, LocalDateTime now) {
		if (status != null && status != this.status) {
			this.status = status;
			this.closedAt = isClosed(status) ? now : null;
		}
		if (category != null) {
			this.category = category;
		}
		if (priority != null) {
			this.priority = priority;
		}
		if (assigneeName != null) {
			String normalized = assigneeName.trim();
			this.assigneeName = normalized.isEmpty() ? null : normalized;
		}
	}

	/** 첫 답변 시각은 최초 한 번만 기록한다. */
	public void markAnswered(LocalDateTime now) {
		if (this.firstAnsweredAt == null) {
			this.firstAnsweredAt = now;
		}
	}

	/** 답변과 동시에 완료 처리할 때 사용한다. */
	public void markDone(LocalDateTime now) {
		update(FeedbackStatus.DONE, null, null, null, now);
	}

	/** 열람자의 우선 처리 요청을 토글한다. 해제는 요청자 본인만 할 수 있다. */
	public void togglePriorityRequest(User requester) {
		if (this.priorityRequested) {
			if (this.priorityRequester != null && !this.priorityRequester.getId().equals(requester.getId())) {
				throw new IllegalStateException("우선 처리 요청은 요청한 계정만 해제할 수 있습니다.");
			}
			this.priorityRequested = false;
			this.priorityRequester = null;
			return;
		}

		this.priorityRequested = true;
		this.priority = Priority.HIGH;
		this.priorityRequester = requester;
	}

	private boolean isClosed(FeedbackStatus status) {
		return status == FeedbackStatus.DONE || status == FeedbackStatus.REJECTED;
	}
}
