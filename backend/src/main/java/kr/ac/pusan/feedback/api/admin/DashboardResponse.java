package kr.ac.pusan.feedback.api.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
		long totalFeedbacks,
		Map<String, Long> countByStatus,
		Map<String, Long> countByType,
		long userCount,
		double averageResolutionHours,
		List<TrendPoint> trend
) {
	public record TrendPoint(LocalDate date, long count) {
	}
}
