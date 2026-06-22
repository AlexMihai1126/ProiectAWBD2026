package ro.fmi.awbd.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ro.fmi.awbd.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@IntegrationTest
class ShootControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "client", roles = "CLIENT")
    void clientCanListOwnShoots() throws Exception {
        mockMvc.perform(get("/shoots"))
                .andExpect(status().isOk())
                .andExpect(view().name("shoot/list"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanOpenCreateShootForm() throws Exception {
        mockMvc.perform(get("/shoots/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("shoot/form"));
    }
}
