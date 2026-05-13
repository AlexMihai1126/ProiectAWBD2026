package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.ClientEntity;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
}
