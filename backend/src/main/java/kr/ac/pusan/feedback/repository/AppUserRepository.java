package kr.ac.pusan.feedback.repository;

import java.util.Optional;
import kr.ac.pusan.feedback.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByEmail(String email);
}
