package ro.fmi.awbd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ro.fmi.awbd.model.dto.request.ClientCreateRequest;
import ro.fmi.awbd.repository.security.UserRepository;
import ro.fmi.awbd.service.ClientService;
import ro.fmi.awbd.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class DatabaseSmokeTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void seededUsersExist() {
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(userRepository.findByUsername("admin")).isPresent();
        assertThat(userRepository.findByUsername("guest")).isPresent();
    }

    @Test
    void clientCrudWorks() {
        var created = clientService.createClient(ClientCreateRequest.builder()
                .name("Smoke Test Client")
                .email("smoke@example.com")
                .phone("0700000000")
                .build());

        assertThat(created.getId()).isNotNull();
        assertThat(clientService.getClient(created.getId()).getName()).isEqualTo("Smoke Test Client");

        var page = clientService.getClients(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
}
