package kr.ac.pusan.feedback.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import kr.ac.pusan.feedback.domain.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 계정")
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

	private static final Instant CREATED_AT = Instant.parse("2026-09-01T09:00:00Z");

	@Operation(summary = "계정 목록 조회")
	@GetMapping
	List<AccountResponse> findAll() {
		return List.of(sampleAccount(4L));
	}

	@Operation(summary = "계정 역할·승인 상태 변경")
	@PatchMapping("/{accountId}")
	AccountResponse update(
			@PathVariable Long accountId,
			@Valid @RequestBody UpdateAccountRequest request
	) {
		AccountResponse original = sampleAccount(accountId);
		return new AccountResponse(
				original.id(),
				original.email(),
				original.displayName(),
				request.role() == null ? original.role() : request.role(),
				request.approved() == null ? original.approved() : request.approved(),
				original.createdAt()
		);
	}

	private AccountResponse sampleAccount(Long id) {
		return new AccountResponse(id, "pending@demo.local", "승인 대기 사용자", Role.VIEWER, false, CREATED_AT);
	}
}
