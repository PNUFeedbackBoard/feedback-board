package kr.ac.pusan.feedback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "feedbacks")
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private AppUser author;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, length = 5000)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private FeedbackType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FeedbackStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "reported_priority", nullable = false, length = 10)
	private Priority reportedPriority;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Priority priority;

	@Column(name = "priority_requested", nullable = false)
	private boolean priorityRequested;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Feedback() {
	}

	public Feedback(
			Project project,
			AppUser author,
			String title,
			String content,
			FeedbackType type,
			FeedbackStatus status,
			Priority reportedPriority,
			Priority priority,
			boolean priorityRequested
	) {
		this.project = project;
		this.author = author;
		this.title = title;
		this.content = content;
		this.type = type;
		this.status = status;
		this.reportedPriority = reportedPriority;
		this.priority = priority;
		this.priorityRequested = priorityRequested;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public AppUser getAuthor() {
		return author;
	}

	public String getTitle() {
		return title;
	}

	public FeedbackType getType() {
		return type;
	}

	public FeedbackStatus getStatus() {
		return status;
	}

	public Priority getReportedPriority() {
		return reportedPriority;
	}

	public Priority getPriority() {
		return priority;
	}
}
