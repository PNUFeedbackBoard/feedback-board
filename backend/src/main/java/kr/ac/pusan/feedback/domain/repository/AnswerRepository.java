package kr.ac.pusan.feedback.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.pusan.feedback.domain.Answer;

/**
 * 답변 리포지토리.
 *
 * <p>아래 두 메서드는 B 가 관리용 피드백 조회(목록의 answered 여부, 상세의 답변 내용)를 위해
 * 읽기 전용으로 추가했다. 답변 등록·수정에 필요한 메서드는 A 가 3단계에서 추가한다.
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {

	/** 피드백 1건의 답변. 관리용 상세(GET /api/admin/feedbacks/{id})에서 쓴다. */
	Optional<Answer> findByFeedbackId(Long feedbackId);

	/**
	 * 주어진 피드백 id들 중 답변이 달린 id만 돌려준다.
	 * 관리용 목록에서 건마다 따로 조회하지 않고 한 번에 answered 여부를 구하기 위한 것이다(N+1 방지).
	 */
	@Query("SELECT a.feedback.id FROM Answer a WHERE a.feedback.id IN :feedbackIds")
	List<Long> findFeedbackIdsWithAnswer(@Param("feedbackIds") List<Long> feedbackIds);

	// TODO(A, 3단계): 답변 등록·수정(PUT /api/admin/feedbacks/{id}/answer)에 필요한 메서드가 있으면 더한다.
}
