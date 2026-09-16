package kr.ac.pusan.feedback.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.pusan.feedback.domain.Feedback;

/**
 * 피드백 리포지토리.
 *
 * <p>0-1단계에서는 비워 둔다. 소유는 A 이며, B 가 조회 메서드를 추가할 때는
 * 커밋 전에 공유한다(기획안 13-4).
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	// TODO(A, 1단계): 내 문의 목록·상세 조회 메서드를 추가한다.
	// TODO(B, 1/3단계): 관리용 목록(필터·정렬)과 대시보드 집계 메서드를 추가한다.
}
