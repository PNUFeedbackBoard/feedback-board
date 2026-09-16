package kr.ac.pusan.feedback.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Operation(summary = "현재 로그인 사용자 조회")
	@GetMapping("/me")
	CurrentUserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
		return CurrentUserResponse.from(currentUser);
	}
}
