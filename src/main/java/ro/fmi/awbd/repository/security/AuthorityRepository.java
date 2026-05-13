package ro.fmi.awbd.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.security.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
}
