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
	 * 구글 계정 고유 식별자(sub). 데모 로그인으로 만든 계정은 null 이다.
	 * 7단계부터는 구글 로그인 성공 시 이 값으로 계정을 찾는다 — 없으면 이메일로 찾아 연결한다.
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

	public void update(Role role, UserStatus status) {
		if (role != null) {
			this.role = role;
		}
		if (status != null) {
			this.status = status;
		}
	}

	/**
	 * 구글 로그인(7단계)에서 처음 googleSub 로 못 찾고 이메일로 기존 계정을 찾았을 때,
	 * 그 계정에 googleSub 을 연결해 다음부터는 바로 찾을 수 있게 한다.
	 */
	public void linkGoogleSub(String googleSub) {
		this.googleSub = googleSub;
	}
}
