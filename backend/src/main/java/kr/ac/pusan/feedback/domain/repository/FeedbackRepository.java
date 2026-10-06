package kr.ac.pusan.feedback.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.ac.pusan.feedback.domain.Feedback;

/**
 * 피드백 리포지토리.
 *
 * <p>0-1단계에서는 비워 둔다. 소유는 A 이며, B 가 조회 메서드를 추가할 때는
 * 커밋 전에 공유한다(기획안 13-4).
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	@EntityGraph(attributePaths = { "project", "author" })
	List<Feedback> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

	@EntityGraph(attributePaths = { "project", "author" })
	Optional<Feedback> findByIdAndAuthorId(Long id, Long authorId);

	@EntityGraph(attributePaths = { "project", "author" })
	@Query("select f from Feedback f")
	List<Feedback> findAllWithAssociations();

	@EntityGraph(attributePaths = { "project", "author" })
	@Query("select f from Feedback f where f.id = :id")
	Optional<Feedback> findByIdWithAssociations(Long id);
}
