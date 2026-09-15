package kr.ac.pusan.feedback.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개발자 답변. 기획안 6-4, 8장.
 *
 * <p>피드백 1건당 1건만 등록할 수 있으므로 feedback_id 에 유니크 제약을 건다.
 * 등록 후 수정할 수 있으며, 수정 시에는 알림을 보내지 않는다(기획안 7장).
 * 비회원 피드백은 답변 대상이 아니다.
 */
@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "feedback_id", nullable = false, unique = true,
			foreignKey = @ForeignKey(name = "fk_answers_feedback"))
	private Feedback feedback;

	/** 답변을 작성한 개발자 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_answers_author"))
	private User author;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	private Answer(Feedback feedback, User author, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.feedback = feedback;
		this.author = author;
		this.content = content;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	// TODO(A, 3단계): 답변 수정(PUT /api/admin/feedbacks/{id}/answer)용 도메인 메서드는 담당자가 추가한다.
}
