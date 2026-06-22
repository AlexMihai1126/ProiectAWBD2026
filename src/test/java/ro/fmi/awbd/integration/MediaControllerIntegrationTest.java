package ro.fmi.awbd.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ro.fmi.awbd.model.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.dto.request.MediaCreateRequest;
import ro.fmi.awbd.model.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.enums.MediaType;
import ro.fmi.awbd.repository.ClientRepository;
import ro.fmi.awbd.repository.security.UserRepository;
import ro.fmi.awbd.service.LocationService;
import ro.fmi.awbd.service.MediaService;
import ro.fmi.awbd.service.ShootService;
import ro.fmi.awbd.support.IntegrationTest;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@IntegrationTest
class MediaControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private LocationService locationService;
    @Autowired private ShootService shootService;
    @Autowired private MediaService mediaService;

    private Long shootId;
    private Long mediaId;

    @BeforeEach
    void createMediaForClient() {
        var owner = userRepository.findByUsername("admin").orElseThrow();
        var account = userRepository.findByUsername("client").orElseThrow();
        var client = clientRepository.findByUserId(account.getId()).orElseThrow();
        var location = locationService.createLocation(LocationCreateRequest.builder()
                .name("Media integration studio " + System.nanoTime())
                .build());
        var shoot = shootService.createShoot(ShootCreateRequest.builder()
                .title("Media integration shoot")
                .startAt(OffsetDateTime.now().plusDays(1))
                .ownerId(owner.getId())
                .locationId(location.getId())
                .clientId(client.getId())
                .build());
        var media = mediaService.createMedia(shoot.getId(), MediaCreateRequest.builder()
                .mediaType(MediaType.EDITED_PHOTO)
                .fileRef("gallery/final-photo.jpg")
                .rating(5)
                .build());
        shootId = shoot.getId();
        mediaId = media.getId();
    }

    @Test
    @WithMockUser(username = "client", roles = "CLIENT")
    void clientCanViewMediaFromOwnShoot() throws Exception {
        mockMvc.perform(get("/shoots/{shootId}/media/{mediaId}", shootId, mediaId))
                .andExpect(status().isOk())
                .andExpect(view().name("media/detail"));
    }

    @Test
    @WithMockUser(username = "client2", roles = "CLIENT")
    void clientCannotViewMediaFromAnotherClientsShoot() throws Exception {
        mockMvc.perform(get("/shoots/{shootId}/media/{mediaId}", shootId, mediaId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanViewMediaDetails() throws Exception {
        mockMvc.perform(get("/shoots/{shootId}/media/{mediaId}", shootId, mediaId))
                .andExpect(status().isOk())
                .andExpect(view().name("media/detail"));
    }
}
