package kr.ac.pusan.feedback;

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
		mockMvc.perform(post("/api/dev/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"account\":\"dev\"}"))
				.andExpect(status().isNotFound());
	}
}
