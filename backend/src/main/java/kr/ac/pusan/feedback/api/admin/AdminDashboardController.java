package kr.ac.pusan.feedback.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 대시보드")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

	@Operation(summary = "전체 현황 대시보드 조회")
	@GetMapping
	DashboardResponse dashboard() {
		return new DashboardResponse(
				40,
				Map.of("RECEIVED", 10L, "IN_PROGRESS", 10L, "DONE", 10L, "REJECTED", 10L),
				Map.of("FEATURE_REQUEST", 14L, "BUG_REPORT", 13L, "OTHER", 13L),
				3,
				28.5,
				List.of(
						new DashboardResponse.TrendPoint(LocalDate.of(2026, 8, 31), 17),
						new DashboardResponse.TrendPoint(LocalDate.of(2026, 9, 7), 23)
				)
		);
	}
}
