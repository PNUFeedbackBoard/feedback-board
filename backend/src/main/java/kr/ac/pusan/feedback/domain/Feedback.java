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
	 * <p>TODO(A, 1단계): 상태 변경 도메인 메서드가 생기면 시드도 그것을 쓰도록 바꾸고 이 팩터리를 지운다.
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

	// TODO(A/B, 1단계 이후): 상태 변경, 유형 변경, 중요도 확정, 우선 처리 요청 토글,
	//                        firstAnsweredAt / closedAt 기록 메서드는 각 기능 담당자가 여기에 추가한다.
	//                        Feedback 엔티티의 소유는 A 이므로 B 는 수정 전에 공유한다(기획안 13-4).
}
