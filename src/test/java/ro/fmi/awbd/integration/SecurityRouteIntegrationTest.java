package ro.fmi.awbd.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ro.fmi.awbd.support.IntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class SecurityRouteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loggedOutUserIsSentToLogin() throws Exception {
        mockMvc.perform(get("/shoots"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "client", roles = "CLIENT")
    void clientCanOpenDashboardAndOwnShootsList() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/shoots"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "client", roles = "CLIENT")
    void clientCannotOpenAdminGetRoutes() throws Exception {
        mockMvc.perform(get("/clients")).andExpect(status().isForbidden());
        mockMvc.perform(get("/locations")).andExpect(status().isForbidden());
        mockMvc.perform(get("/gear")).andExpect(status().isForbidden());
        mockMvc.perform(get("/stats")).andExpect(status().isForbidden());
        mockMvc.perform(get("/shoots/new")).andExpect(status().isForbidden());
        mockMvc.perform(get("/shoots/1/edit")).andExpect(status().isForbidden());
        mockMvc.perform(get("/shoots/1/media/new")).andExpect(status().isForbidden());
        mockMvc.perform(get("/shoots/1/invoice/new")).andExpect(status().isForbidden());
        mockMvc.perform(get("/h2-console/")).andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "client", roles = "CLIENT")
    void clientCannotPerformWrites() throws Exception {
        mockMvc.perform(post("/shoots").with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/clients").with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/shoots/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
