package ro.fmi.awbd.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ro.fmi.awbd.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@IntegrationTest
class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "guest", roles = "GUEST")
    void guestCanOpenStatsPageWithoutQuery() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats/index"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminSeesErrorWhenFromIsAfterTo() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("from", "2026-12-31T23:59:59Z")
                        .param("to", "2026-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats/index"))
                .andExpect(model().attributeExists("statsError"));
    }
}
