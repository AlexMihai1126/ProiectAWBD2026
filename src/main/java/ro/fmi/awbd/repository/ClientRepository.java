package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.ClientEntity;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndIdNot(Long userId, Long id);

    Optional<ClientEntity> findByUserId(Long userId);
}
