package ro.fmi.awbd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.fmi.awbd.model.dto.request.GearCreateRequest;
import ro.fmi.awbd.model.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.enums.GearType;
import ro.fmi.awbd.model.enums.ShootStatus;
import ro.fmi.awbd.repository.security.UserRepository;
import ro.fmi.awbd.service.GearService;
import ro.fmi.awbd.service.LocationService;
import ro.fmi.awbd.service.ShootService;
import ro.fmi.awbd.support.IntegrationTest;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShootCreateSmokeTest {

    @Autowired
    private ShootService shootService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private GearService gearService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void createShootWithGear() {
        var owner = userRepository.findByUsername("admin").orElseThrow();
        var location = locationService.createLocation(LocationCreateRequest.builder().name("Studio A").build());
        var gear = gearService.createGear(GearCreateRequest.builder()
                .type(GearType.CAMERA_BODY)
                .brand("Canon")
                .model("R5")
                .ownerId(owner.getId())
                .build());

        var shoot = shootService.createShoot(ShootCreateRequest.builder()
                .title("Wedding")
                .status(ShootStatus.PLANNED)
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().plusDays(1).plusHours(2))
                .ownerId(owner.getId())
                .locationId(location.getId())
                .gearItemIds(Set.of(gear.getId()))
                .build());

        assertThat(shoot.getId()).isNotNull();
        assertThat(shootService.getShootById(shoot.getId()).getTitle()).isEqualTo("Wedding");
    }
}
