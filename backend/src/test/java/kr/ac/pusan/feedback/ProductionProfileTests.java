package kr.ac.pusan.feedback;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("prod")
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
class ProductionProfileTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void demoLoginDoesNotExistOutsideDevelopmentProfile() throws Exception {
		mockMvc.perform(post("/api/dev/login").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"account\":\"dev\"}"))
				.andExpect(status().isNotFound());
	}

    @Test
    void productionHasNoPreviewAndRequiresCsrf() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("previewEnabled").value(false));
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
    }
}
