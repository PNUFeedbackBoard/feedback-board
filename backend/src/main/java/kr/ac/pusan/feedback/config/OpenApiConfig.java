package kr.ac.pusan.feedback.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI feedbackOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("통합 피드백 게시판 API")
						.description("부산대 AI융합교육원 시스템 피드백 API")
						.version("v1"));
	}
}
