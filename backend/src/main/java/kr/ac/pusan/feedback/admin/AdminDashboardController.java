package kr.ac.pusan.feedback.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.pusan.feedback.admin.dto.DashboardResponse;
import kr.ac.pusan.feedback.common.StubData;

/**
 * 전체 현황 대시보드 API. 기획안 6-2, 9장.
 *
 * <p>0-1단계 스텁이다. 지표 6종을 고정값으로 돌려주며 집계 쿼리를 돌리지 않는다.
 * dailyTrend 는 선 그래프를 그릴 수 있도록 30일치를 담고 있다.
 */
@Tag(name = "관리", description = "개발자·열람자용 API")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

	// TODO(B, 3단계): 실제 집계로 교체한다. 지표 산출 방식은 기획안 6-2의 표를 따른다.
	//                 averageProcessingHours 는 DONE 과 REJECTED 만 집계하고,
	//                 userCount 는 피드백을 1건 이상 등록한 고유 이용자 수다.
	@Operation(summary = "전체 현황 조회",
			description = "조회 전용 화면이다. 상태별 건수, 프로젝트별 교차 집계, 유형별 비율, 30일 추이, "
					+ "평균 처리 소요 시간, 이용자 수, 우선 처리 요청 목록, 최근 접수 10건을 한 번에 내려준다.")
	@GetMapping
	public DashboardResponse dashboard() {
		return StubData.dashboard();
	}
}
