package kr.ac.pusan.feedback.admin.dto;

import java.util.List;
import java.util.Map;

import kr.ac.pusan.feedback.common.enums.FeedbackCategory;
import kr.ac.pusan.feedback.common.enums.FeedbackStatus;

/**
 * 전체 현황 대시보드. GET /api/admin/dashboard
 *
 * <p>지표 6종의 산출 방식은 기획안 6-2의 표와 같다. 조회 전용 화면이므로 변경용 필드는 없다.
 *
 * <p>statusCounts 와 counts 는 Map 이며 JSON 으로는 상태 이름이 키가 된다.
 * 예: {@code {"RECEIVED":14,"IN_PROGRESS":9,"DONE":12,"REJECTED":5}}
 * 네 가지 상태를 항상 모두 담는다. 건수가 0 이어도 키를 빼지 않는다.
 */
public record DashboardResponse(

		// 상태별 건수 합계
		Map<FeedbackStatus, Long> statusCounts,

		// 프로젝트와 상태의 교차 집계. 누적 막대
		List<ProjectStatusCount> projectStatusCounts,

		// 유형별 건수와 비율. 도넛
		List<CategoryCount> categoryCounts,

		// 일자별 접수 건수. 기본 30일. 선 그래프
		List<DailyCount> dailyTrend,

		// closedAt - createdAt 의 평균 시간. DONE 과 REJECTED 만 집계한다
		double averageProcessingHours,

		// 피드백을 1건 이상 등록한 고유 이용자 수
		UserCount userCount,

		// 우선 처리 요청된 피드백 목록
		List<AdminFeedbackSummary> priorityRequested,

		// 최근 접수 10건
		List<AdminFeedbackSummary> recent
) {

	/** 프로젝트 1건의 상태별 건수 */
	public record ProjectStatusCount(
			String projectCode,
			String projectName,
			Map<FeedbackStatus, Long> counts
	) {
	}

	/** 유형 1건의 건수와 비율. ratio 는 0~1 소수다 */
	public record CategoryCount(
			FeedbackCategory category,
			long count,
			double ratio
	) {
	}

	/** 하루치 접수 건수. date 는 "2026-09-15" 형식 문자열이다 */
	public record DailyCount(
			String date,
			long count
	) {
	}

	/** 회원과 비회원 이용자 수. 비회원은 별도로 집계한다 */
	public record UserCount(
			long member,
			long guest
	) {
	}
}
