package kr.ac.pusan.feedback.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.pusan.feedback.domain.Project;

/**
 * 프로젝트 리포지토리.
 *
 * <p>0-1단계에서는 시드 데이터와 피드백 등록이 쓰는 findByCode 만 둔다.
 * 조회 메서드는 각 담당자가 자기 기능과 함께 추가한다.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

	/** 시드 데이터 중복 확인과 피드백 등록 시 projectCode 해석에 쓴다 */
	Optional<Project> findByCode(String code);

	// TODO(A/B, 1단계 이후): 정렬 조회 등 필요한 메서드는 담당자가 추가한다.
}
