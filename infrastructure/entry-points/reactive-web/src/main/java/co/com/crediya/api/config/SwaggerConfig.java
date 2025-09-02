package co.com.crediya.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Requests Microservice - CrediYa",
        version = "1.0",
        description = "Microservice for handling user requests for a credit"
))
public class SwaggerConfig {
}
