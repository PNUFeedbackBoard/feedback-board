package kr.ac.pusan.feedback.domain.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.ac.pusan.feedback.common.enums.Role;
import kr.ac.pusan.feedback.common.enums.UserStatus;
import kr.ac.pusan.feedback.domain.User;

/**
 * 계정 리포지토리.
 *
 * <p>0-1단계에서는 시드 데이터와 데모 로그인이 쓰는 findByEmail 만 둔다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/** 데모 로그인(POST /api/dev/login)과 시드 데이터가 쓴다 */
	Optional<User> findByEmail(String email);
	Optional<User> findByGoogleSub(String googleSub);

	/** 우선 처리 요청 알림(기획안 7장) 수신 대상 — 개발자 전원. B가 6단계에 추가했다. */
	List<User> findAllByRoleAndStatus(Role role, UserStatus status);

	/**
	 * 계정 관리 변경을 직렬화한다. 서로 다른 개발자를 동시에 강등하는 두 요청도 같은 행들을
	 * 잠그므로, 둘 다 오래된 활성 개발자 수를 보고 통과하는 경쟁 상태를 막는다.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u order by u.id")
	List<User> findAllForAccountUpdate();
}
