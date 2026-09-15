package kr.ac.pusan.feedback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드백 대상 시스템. 기획안 1-2, 8장.
 *
 * <p>목록은 DB 에서 관리하며 시드 데이터로 5건이 들어간다.
 */
@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** 진입 링크의 ?site= 값으로 쓰는 식별자. 예: codeplace */
	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	/** 화면에 표시하는 이름. 예: 코드플레이스 */
	@Column(name = "name", nullable = false, length = 100)
	private String name;

	/** 하단 디스크에 쓰는 로고 이미지 주소. 미결정 사항이라 null 을 허용한다 */
	@Column(name = "logo_url", length = 500)
	private String logoUrl;

	/** 해당 시스템의 주소 */
	@Column(name = "site_url", length = 500)
	private String siteUrl;

	/** 하단 디스크의 배치 순서 */
	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Builder
	private Project(String code, String name, String logoUrl, String siteUrl, int sortOrder) {
		this.code = code;
		this.name = name;
		this.logoUrl = logoUrl;
		this.siteUrl = siteUrl;
		this.sortOrder = sortOrder;
	}
}
