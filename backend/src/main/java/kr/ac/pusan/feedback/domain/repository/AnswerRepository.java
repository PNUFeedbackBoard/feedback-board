package kr.ac.pusan.feedback.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.pusan.feedback.domain.Answer;

/**
 * 답변 리포지토리.
 *
 * <p>0-1단계에서는 비워 둔다.
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {

	Optional<Answer> findByFeedbackId(Long feedbackId);

	List<Answer> findAllByFeedbackIdIn(Collection<Long> feedbackIds);
}
