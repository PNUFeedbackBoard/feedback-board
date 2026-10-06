package kr.ac.pusan.feedback.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
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
	 * 이 메서드는 조건에 맞는 전체 목록만 project·author·assignee 를 함께 읽어 돌려준다.
	 * (지금 분량(수십~수백 건)에서는 문제없고, 느려지면 그때 페이지·정렬을 쿼리로 내린다.)
	 *
	 * <p>{@code answered}(답변 유무)는 자바에서 거르지 않고 EXISTS 서브쿼리로 DB가 직접 거른다.
	 * "일단 다 가져온 뒤 자바에서 버리는" 방식보다, 맞지 않는 행은 애초에 서버로 전송조차 안 되게 하는 편이
	 * 데이터가 많아져도 느려지지 않는다.
	 */
	@Query("""
			SELECT f FROM Feedback f
			JOIN FETCH f.project p
			LEFT JOIN FETCH f.author a
			LEFT JOIN FETCH f.assignee asg
			WHERE (:projectCode IS NULL OR p.code = :projectCode)
			  AND (:status IS NULL OR f.status = :status)
			  AND (:category IS NULL OR f.category = :category)
			  AND (:authorType IS NULL OR f.authorType = :authorType)
			  AND (:fromInclusive IS NULL OR f.createdAt >= :fromInclusive)
			  AND (:toExclusive IS NULL OR f.createdAt < :toExclusive)
			  AND (
			        :answered IS NULL
			        OR (:answered = true AND EXISTS (SELECT 1 FROM Answer ans WHERE ans.feedback = f))
			        OR (:answered = false AND NOT EXISTS (SELECT 1 FROM Answer ans WHERE ans.feedback = f))
			      )
			""")
	List<Feedback> searchForAdmin(
			@Param("projectCode") String projectCode,
			@Param("status") FeedbackStatus status,
			@Param("category") FeedbackCategory category,
			@Param("authorType") AuthorType authorType,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toExclusive") LocalDateTime toExclusive,
			@Param("answered") Boolean answered);

	/** 관리용 상세 조회. project·author·assignee 를 함께 읽어 컨트롤러에서 지연 로딩 예외가 나지 않게 한다. */
	@Query("""
			SELECT f FROM Feedback f
			JOIN FETCH f.project
			LEFT JOIN FETCH f.author
			LEFT JOIN FETCH f.assignee
			WHERE f.id = :id
			""")
	Optional<Feedback> findDetailById(@Param("id") Long id);

	// ------------------------------------------------------------------
	// 대시보드 집계 (기획안 6-2, 9장). B 가 추가했다.
	//
	// 전체를 자바로 가져와 세는 대신 DB 의 GROUP BY 로 집계한다. 단, 평균 처리 시간은
	// H2 와 PostgreSQL(운영)의 날짜 함수 이름이 달라서 호환성 문제가 생기기 쉬우므로,
	// createdAt·closedAt 쌍만 가져와 자바에서 Duration 으로 계산한다 — 지금 분량에서는
	// 문제없고, 느려지면 5단계("대시보드 집계 쿼리 정리")에서 DB 함수로 내린다.
	// ------------------------------------------------------------------

	/** 상태별 건수(지표 1). 건수가 0인 상태는 아예 행이 없다 — 컨트롤러가 네 상태를 0으로 채워 넣는다. */
	@Query("SELECT f.status AS status, COUNT(f) AS count FROM Feedback f GROUP BY f.status")
	List<StatusCount> countByStatus();

	/** 프로젝트 × 상태 교차 집계(지표 2). 피드백이 0건인 프로젝트는 행이 없다. */
	@Query("""
			SELECT p.code AS projectCode, f.status AS status, COUNT(f) AS count
			FROM Feedback f JOIN f.project p
			GROUP BY p.code, f.status
			""")
	List<ProjectStatusCount> countByProjectAndStatus();

	/** 유형별 건수(지표 3). ratio 계산은 컨트롤러에서 전체 건수로 나눠서 한다. */
	@Query("SELECT f.category AS category, COUNT(f) AS count FROM Feedback f GROUP BY f.category")
	List<CategoryCount> countByCategory();

	/** 기간별 추이(지표 4)에 쓸 등록 시각 원본. 날짜별로 묶는 건 컨트롤러(자바)가 한다. */
	@Query("SELECT f.createdAt FROM Feedback f WHERE f.createdAt >= :from")
	List<LocalDateTime> findCreatedAtFrom(@Param("from") LocalDateTime from);

	/** 평균 처리 소요 시간(지표 5)의 재료. status 는 호출자가 DONE·REJECTED 를 넘긴다. */
	@Query("""
			SELECT f.createdAt AS createdAt, f.closedAt AS closedAt
			FROM Feedback f
			WHERE f.status IN :statuses AND f.closedAt IS NOT NULL
			""")
	List<ProcessingDuration> findProcessingDurations(@Param("statuses") List<FeedbackStatus> statuses);

	/** 이용자 수(지표 6) 중 회원 쪽. 같은 사람이 여러 건 등록해도 한 명으로 센다. */
	@Query("SELECT COUNT(DISTINCT f.author) FROM Feedback f WHERE f.authorType = :authorType")
	long countDistinctAuthorsByAuthorType(@Param("authorType") AuthorType authorType);

	/**
	 * 이용자 수(지표 6) 중 비회원 쪽. 비회원은 계정이 없어 "같은 사람"을 구분할 방법이 없으므로,
	 * 건수 자체를 집계한다(기획안 6-2 "비회원은 별도 집계"를 이렇게 해석했다).
	 */
	@Query("SELECT COUNT(f) FROM Feedback f WHERE f.authorType = :authorType")
	long countByAuthorType(@Param("authorType") AuthorType authorType);

	/** 우선 처리 요청 목록. 대시보드 하단에 쓴다(기획안 6-2). */
	@Query("""
			SELECT f FROM Feedback f
			JOIN FETCH f.project
			LEFT JOIN FETCH f.author
			LEFT JOIN FETCH f.assignee
			WHERE f.priorityRequested = true
			ORDER BY f.createdAt DESC
			""")
	List<Feedback> findPriorityRequested();

	/** 최근 접수 N건. 대시보드 하단에 쓴다(기획안 6-2). 호출자가 PageRequest.of(0, 10) 등을 넘긴다. */
	@Query("""
			SELECT f FROM Feedback f
			JOIN FETCH f.project
			LEFT JOIN FETCH f.author
			LEFT JOIN FETCH f.assignee
			ORDER BY f.createdAt DESC
			""")
	List<Feedback> findRecent(Pageable pageable);

	/** 상태별 건수 집계 결과 1행(Spring Data 인터페이스 프로젝션). */
	interface StatusCount {
		FeedbackStatus getStatus();

		long getCount();
	}

	/** 프로젝트 × 상태 집계 결과 1행. */
	interface ProjectStatusCount {
		String getProjectCode();

		FeedbackStatus getStatus();

		long getCount();
	}

	/** 유형별 집계 결과 1행. */
	interface CategoryCount {
		FeedbackCategory getCategory();

		long getCount();
	}

	/** 처리 소요 시간 계산 재료 1행. */
	interface ProcessingDuration {
		LocalDateTime getCreatedAt();

		LocalDateTime getClosedAt();
	}
}
