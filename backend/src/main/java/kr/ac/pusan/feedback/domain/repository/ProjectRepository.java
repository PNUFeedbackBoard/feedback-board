package kr.ac.pusan.feedback.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.pusan.feedback.domain.Project;

/**
 * 프로젝트 리포지토리.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

	/** 시드 데이터 중복 확인과 피드백 등록 시 projectCode 해석에 쓴다 */
	Optional<Project> findByCode(String code);

	/** GET /api/projects 가 쓴다. 메서드 이름 그대로 sortOrder 오름차순 조회다(기획안 9장). */
	List<Project> findAllByOrderBySortOrderAsc();
}
