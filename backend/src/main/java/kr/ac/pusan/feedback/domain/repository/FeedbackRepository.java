package kr.ac.pusan.feedback.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.pusan.feedback.common.enums.AuthorType;
import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;
import kr.ac.pusan.feedback.domain.Feedback;

/**
 * 피드백 리포지토리.
 *
 * <p>소유는 A 이며, B 가 조회 메서드를 추가할 때는 커밋 전에 공유한다(기획안 13-4).
 * 아래 두 메서드는 B 가 1단계 관리용 API 를 위해 추가했다.
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	// TODO(A, 1단계): 내 문의 목록·상세 조회 메서드를 추가한다.

	/**
	 * 관리용 목록 조회(기획안 9장, 6-3). 필터는 전부 선택값이며 null 이면 그 조건을 걸지 않는다.
	 *
	 * <p>정렬과 페이지는 여기서 하지 않는다. "우선 처리 요청이 항상 1순위"라는 기획안 4-5 규칙은
	 * DB 정렬보다 자바 {@code Comparator} 로 처리하는 편이 더 명확하고 테스트하기 쉬워서,
	 * 이 메서드는 조건에 맞는 전체 목록만 project·author 를 함께 읽어 돌려준다.
	 * (지금 분량(수십~수백 건)에서는 문제없고, 느려지면 그때 페이지·정렬을 쿼리로 내린다.)
	 */
	@Query("""
			SELECT f FROM Feedback f
			JOIN FETCH f.project p
			LEFT JOIN FETCH f.author a
			WHERE (:projectCode IS NULL OR p.code = :projectCode)
			  AND (:status IS NULL OR f.status = :status)
			  AND (:category IS NULL OR f.category = :category)
			  AND (:authorType IS NULL OR f.authorType = :authorType)
			  AND (:fromInclusive IS NULL OR f.createdAt >= :fromInclusive)
			  AND (:toExclusive IS NULL OR f.createdAt < :toExclusive)
			""")
	List<Feedback> searchForAdmin(
			@Param("projectCode") String projectCode,
			@Param("status") FeedbackStatus status,
			@Param("category") FeedbackCategory category,
			@Param("authorType") AuthorType authorType,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toExclusive") LocalDateTime toExclusive);

	/** 관리용 상세 조회. project·author 를 함께 읽어 컨트롤러에서 지연 로딩 예외가 나지 않게 한다. */
	@Query("""
			SELECT f FROM Feedback f
			JOIN FETCH f.project
			LEFT JOIN FETCH f.author
			WHERE f.id = :id
			""")
	Optional<Feedback> findDetailById(@Param("id") Long id);

	// TODO(B, 3단계): 대시보드 집계에 필요한 조회 메서드를 추가한다.
}
