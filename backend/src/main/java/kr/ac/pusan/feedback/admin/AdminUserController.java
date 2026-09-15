package kr.ac.pusan.feedback.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.admin.dto.AdminUserResponse;
import kr.ac.pusan.feedback.admin.dto.AdminUserUpdateRequest;
import kr.ac.pusan.feedback.common.StubData;

/**
 * 계정 관리 API. 기획안 3-2, 6-5, 9장.
 *
 * <p>0-1단계 스텁이다. 데모 계정 4종을 고정으로 돌려주며 실제 승인·변경은 하지 않는다.
 * 계정 승인 권한은 모든 개발자가 가진다(기획안 3-1).
 */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

	// TODO(B, 6단계): UserRepository 조회로 교체한다. 승인 대기(PENDING)가 위에 오도록 정렬한다.
	@Operation(summary = "계정 목록 조회",
			description = "개발자 전용이다. 승인 대기 계정의 이메일·이름·가입일을 확인하는 화면에서 쓴다.")
	@GetMapping
	public List<AdminUserResponse> users() {
		return StubData.users();
	}

	// TODO(B, 6단계): 실제 승인·역할 변경으로 교체한다.
	//                 role 과 status 중 null 이 아닌 값만 바꾼다. 없는 id 면 404 를 반환한다.
	@Operation(summary = "계정 승인·역할 변경",
			description = "개발자 전용이다. 보낸 필드만 바꾼다. status 를 ACTIVE 로 보내면 승인, DISABLED 로 보내면 비활성화다.")
	@PatchMapping("/{id}")
	public AdminUserResponse update(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
		return StubData.user(id);
	}
}
