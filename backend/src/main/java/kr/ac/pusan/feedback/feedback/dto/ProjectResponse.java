package kr.ac.pusan.feedback.feedback.dto;

/**
 * 프로젝트 목록 응답. GET /api/projects
 *
 * <p>sortOrder 는 관리용 화면 하단 디스크의 배치 순서다(기획안 6-1).
 * logoUrl 과 siteUrl 은 배포 환경의 프로젝트 정보가 아직 등록되지 않았으면 null 일 수 있다.
 * 화면은 로고 대체 표시를 사용하고 서비스 링크는 숨긴다.
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
