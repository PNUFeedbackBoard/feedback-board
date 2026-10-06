package kr.ac.pusan.feedback.admin;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.admin.dto.AdminUserResponse;
import kr.ac.pusan.feedback.admin.dto.AdminUserUpdateRequest;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.UserRepository;
import kr.ac.pusan.feedback.feedback.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계정 관리 API. 기획안 3-2, 6-5, 9장.
 *
 * <p>승인 대기 계정을 먼저 보여 주며, 계정 승인 권한은 모든 개발자가 가진다(기획안 3-1).
 */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("principal.status == T(kr.ac.pusan.feedback.common.enums.UserStatus).ACTIVE")
public class AdminUserController {

	private final UserRepository userRepository;

	@Operation(summary = "계정 목록 조회",
			description = "개발자 전용이다. 승인 대기 계정의 이메일·이름·가입일을 확인하는 화면에서 쓴다.")
	@GetMapping
	public List<AdminUserResponse> users() {
		return userRepository.findAll().stream()
				.sorted((left, right) -> {
					int status = Integer.compare(statusRank(left.getStatus()), statusRank(right.getStatus()));
					return status != 0 ? status : left.getCreatedAt().compareTo(right.getCreatedAt());
				})
				.map(FeedbackMapper::user)
				.toList();
	}

	@Operation(summary = "계정 승인·역할 변경",
			description = "개발자 전용이다. 보낸 필드만 바꾼다. status 를 ACTIVE 로 보내면 승인, DISABLED 로 보내면 비활성화다.")
	@PatchMapping("/{id}")
	@Transactional
	public AdminUserResponse update(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("계정을 찾을 수 없습니다."));
		user.update(request.role(), request.status());
		return FeedbackMapper.user(user);
	}

	private int statusRank(UserStatus status) {
		return switch (status) {
			case PENDING -> 0;
			case ACTIVE -> 1;
			case DISABLED -> 2;
		};
	}
}
