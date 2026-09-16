package kr.ac.pusan.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiContractIntegrationTests {

	private static final Set<String> HTTP_METHODS = Set.of(
			"get", "post", "put", "patch", "delete", "head", "options", "trace"
	);

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void openApiExposesExactlyTwelveProductOperations() throws Exception {
		String body = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode paths = objectMapper.readTree(body).path("paths");
		long operationCount = 0;
		for (JsonNode pathItem : paths) {
			for (var fields = pathItem.fieldNames(); fields.hasNext(); ) {
				if (HTTP_METHODS.contains(fields.next())) {
					operationCount++;
				}
			}
		}

		assertThat(operationCount).isEqualTo(12);
		assertThat(paths.has("/api/dev/login")).isFalse();
		assertThat(paths.has("/api/me")).isFalse();
	}

	@Test
	void allTwelveStubOperationsReturnOkWithRequiredRoles() throws Exception {
		MockHttpSession user = login("user");
		MockHttpSession viewer = login("viewer");
		MockHttpSession developer = login("dev");

		mockMvc.perform(get("/api/projects")).andExpect(status().isOk());
		mockMvc.perform(post("/api/feedbacks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "category": "BUG",
								  "reportedPriority": "HIGH",
								  "title": "검색 오류",
								  "content": "검색 결과가 화면에 표시되지 않습니다."
								}
								"""))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/me/feedbacks").session(user)).andExpect(status().isOk());
		mockMvc.perform(get("/api/me/feedbacks/1").session(user)).andExpect(status().isOk());

		mockMvc.perform(get("/api/admin/dashboard").session(viewer)).andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/feedbacks").session(viewer)).andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/feedbacks/1").session(viewer)).andExpect(status().isOk());
		mockMvc.perform(post("/api/admin/feedbacks/1/priority-request").session(viewer))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/admin/feedbacks/1")
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"IN_PROGRESS\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(put("/api/admin/feedbacks/1/answer")
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"content\":\"확인했습니다.\",\"markDone\":false}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/users").session(developer)).andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/users/4")
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"VIEWER\",\"status\":\"ACTIVE\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void viewerCanReadAdminApiButCannotModifyFeedback() throws Exception {
		MockHttpSession session = login("viewer");

		mockMvc.perform(get("/api/admin/feedbacks").session(session))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/feedbacks/1")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DONE\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void currentUserResponseComesFromServerSession() throws Exception {
		MockHttpSession session = login("dev");

		mockMvc.perform(get("/api/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("DEVELOPER"));
	}

	private MockHttpSession login(String account) throws Exception {
		return (MockHttpSession) mockMvc.perform(post("/api/dev/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"account\":\"" + account + "\"}"))
				.andExpect(status().isOk())
				.andReturn()
				.getRequest()
				.getSession(false);
	}
}
