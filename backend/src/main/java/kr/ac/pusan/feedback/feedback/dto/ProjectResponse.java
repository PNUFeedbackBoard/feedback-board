package kr.ac.pusan.feedback.feedback.dto;

/**
 * 프로젝트 목록 응답. GET /api/projects
 *
 * <p>sortOrder 는 관리용 화면 하단 디스크의 배치 순서다(기획안 6-1).
 * logoUrl 은 로고 이미지가 미결정 상태라 null 로 내려갈 수 있다(기획안 12장 1번).
 * 화면은 null 인 경우의 대체 표시를 준비해야 한다.
 */
public record ProjectResponse(
		Long id,
		String code,
		String name,
		String logoUrl,
		String siteUrl,
		int sortOrder
) {
}
