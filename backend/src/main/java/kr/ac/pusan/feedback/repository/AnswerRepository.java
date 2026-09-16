package kr.ac.pusan.feedback.repository;

import java.util.Optional;
import kr.ac.pusan.feedback.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
	Optional<Answer> findByFeedbackId(Long feedbackId);
}
