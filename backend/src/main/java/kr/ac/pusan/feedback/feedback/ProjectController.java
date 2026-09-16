package kr.ac.pusan.feedback.feedback;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.common.StubData;
import kr.ac.pusan.feedback.feedback.dto.ProjectResponse;

/**
 * 프로젝트 목록 API. 기획안 9장.
 *
 * <p>0-1단계 스텁이다. 모든 응답은 StubData 의 고정값이며 DB 를 조회하지 않는다.
 */
@Tag(name = "공개", description = "로그인 없이 호출할 수 있는 API")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	// TODO(A, 1단계): ProjectRepository 조회로 교체한다. sortOrder 오름차순으로 정렬한다.
	@Operation(summary = "프로젝트 목록 조회",
			description = "하단 디스크와 작성 화면의 사이트 드롭다운에 쓰는 목록이다. sortOrder 오름차순이다.")
	@GetMapping
	public List<ProjectResponse> projects() {
		return StubData.projects();
	}
}
