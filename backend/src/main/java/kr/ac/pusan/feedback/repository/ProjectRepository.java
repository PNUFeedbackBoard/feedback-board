package kr.ac.pusan.feedback.repository;

import java.util.Optional;
import kr.ac.pusan.feedback.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	Optional<Project> findByCode(String code);
}
