package kr.ac.pusan.feedback.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.pusan.feedback.domain.Answer;

/**
 * 답변 리포지토리.
 *
 * <p>0-1단계에서는 비워 둔다.
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {

	// TODO(A, 3단계): 피드백별 답변 조회(findByFeedbackId 등) 메서드를 추가한다.
}
