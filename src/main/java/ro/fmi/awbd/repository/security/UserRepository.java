package ro.fmi.awbd.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.security.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
