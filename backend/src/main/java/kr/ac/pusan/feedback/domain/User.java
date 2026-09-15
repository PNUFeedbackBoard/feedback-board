package kr.ac.pusan.feedback.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 계정. 기획안 3장, 8장.
 *
 * <p>테이블 이름은 반드시 users 다. user 는 H2 와 PostgreSQL 의 예약어라 사용할 수 없다.
 * 비회원은 계정을 만들지 않으므로 이 테이블에 행이 생기지 않는다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "email", nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	/**
	 * 구글 계정 고유 식별자(sub).
	 * 7단계에서 구글 로그인으로 전환하기 전까지는 데모 계정이라 null 이다.
	 */
	@Column(name = "google_sub", unique = true, length = 255)
	private String googleSub;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private UserStatus status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Builder
	private User(String email, String name, String googleSub, Role role, UserStatus status, LocalDateTime createdAt) {
		this.email = email;
		this.name = name;
		this.googleSub = googleSub;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
	}

	// TODO(B, 6단계): 역할 변경·승인(PATCH /api/admin/users/{id})용 도메인 메서드는 담당자가 추가한다.
}
