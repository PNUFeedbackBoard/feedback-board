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
	}

	@Test
	void allTwelveStubOperationsReturnOkForDeveloper() throws Exception {
		MockHttpSession session = login("dev@demo.local");

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("DEVELOPER"));

		mockMvc.perform(post("/api/feedbacks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "title": "검색 오류",
								  "content": "검색 결과가 표시되지 않습니다.",
								  "type": "BUG_REPORT",
								  "reportedPriority": "HIGH"
								}
								"""))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/feedbacks/my").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/api/feedbacks/my/1").session(session)).andExpect(status().isOk());
		mockMvc.perform(post("/api/feedbacks/1/priority-request").session(session)).andExpect(status().isOk());

		mockMvc.perform(get("/api/admin/feedbacks").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/feedbacks/1").session(session)).andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/feedbacks/1")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"IN_PROGRESS\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(put("/api/admin/feedbacks/1/answer")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"content\":\"확인했습니다.\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/dashboard").session(session)).andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/accounts").session(session)).andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/accounts/4")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"VIEWER\",\"approved\":true}"))
				.andExpect(status().isOk());
	}

	@Test
	void viewerCanReadAdminApiButCannotModifyIt() throws Exception {
		MockHttpSession session = login("viewer@demo.local");

		mockMvc.perform(get("/api/admin/feedbacks").session(session))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/feedbacks/1")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DONE\"}"))
				.andExpect(status().isForbidden());
	}

	private MockHttpSession login(String email) throws Exception {
		return (MockHttpSession) mockMvc.perform(post("/api/dev/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\"}"))
				.andExpect(status().isOk())
				.andReturn()
				.getRequest()
				.getSession(false);
	}
}
