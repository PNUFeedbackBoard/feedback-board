package kr.ac.pusan.feedback.repository;

import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
	long countByStatus(FeedbackStatus status);
}
