package kr.ac.pusan.feedback.feedback;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.domain.Project;
import kr.ac.pusan.feedback.domain.repository.ProjectRepository;
import kr.ac.pusan.feedback.feedback.dto.ProjectResponse;

/**
 * 프로젝트 목록 API. 기획안 9장.
 *
 * <p>1단계에 A 가 실제 조회로 교체했다.
 */
@Tag(name = "공개", description = "로그인 없이 호출할 수 있는 API")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final ProjectRepository projectRepository;

	ProjectController(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Operation(summary = "프로젝트 목록 조회",
			description = "하단 디스크와 작성 화면의 사이트 드롭다운에 쓰는 목록이다. sortOrder 오름차순이다.")
	@GetMapping
	public List<ProjectResponse> projects() {
		return projectRepository.findAllByOrderBySortOrderAsc().stream()
				.map(ProjectController::toResponse)
				.toList();
	}

	private static ProjectResponse toResponse(Project project) {
		return new ProjectResponse(
				project.getId(),
				project.getCode(),
				project.getName(),
				project.getLogoUrl(),
				project.getSiteUrl(),
				project.getSortOrder());
	}
}
