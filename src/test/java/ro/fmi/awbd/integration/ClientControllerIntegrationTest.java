package ro.fmi.awbd.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ro.fmi.awbd.support.IntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "client", roles = "CLIENT")
    void clientCannotListOtherClients() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void activeSortDirectionIsRendered() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fa-arrow-up")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void paginationIsRenderedOnlyOnceBelowTheList() throws Exception {
        mockMvc.perform(get("/clients").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .containsOnlyOnce("aria-label=\"Page navigation\""));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanCreateClient() throws Exception {
        mockMvc.perform(post("/clients")
                        .with(csrf())
                        .param("name", "Integration Client")
                        .param("email", "integration@example.com")
                        .param("phone", "0700111222"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/clients/*"));
    }
}
