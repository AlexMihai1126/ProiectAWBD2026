package ro.fmi.awbd.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.security.Authority;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findByRole(String role);
}
