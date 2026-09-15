package kr.ac.pusan.feedback.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.pusan.feedback.domain.User;

/**
 * 계정 리포지토리.
 *
 * <p>0-1단계에서는 시드 데이터와 데모 로그인이 쓰는 findByEmail 만 둔다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/** 데모 로그인(POST /api/dev/login)과 시드 데이터가 쓴다 */
	Optional<User> findByEmail(String email);

	// TODO(B, 6/7단계): 계정 목록 조회와 구글 sub 조회 메서드는 담당자가 추가한다.
}
