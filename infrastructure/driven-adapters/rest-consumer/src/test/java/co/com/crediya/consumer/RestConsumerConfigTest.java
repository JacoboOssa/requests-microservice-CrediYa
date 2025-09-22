package co.com.crediya.consumer;


import co.com.crediya.consumer.config.RestConsumerConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestConsumerConfigTest {

    private RestConsumerConfig config;

    @BeforeEach
    void setUp() {
        config = new RestConsumerConfig("http://localhost:8081", 5000);
    }

    @Test
    void shouldCreateWebClientWithConfiguredBaseUrl() {
        WebClient.Builder builder = WebClient.builder();

        WebClient client = config.gtWebClient(builder);

    }

    @Test
    void shouldTriggerTimeoutHandlersOnConnection() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setBody("{\"status\":\"ok\"}")
                    .addHeader("Content-Type", "application/json"));

            RestConsumerConfig config = new RestConsumerConfig(server.url("/").toString(), 500);

            WebClient client = config.gtWebClient(WebClient.builder());

            String body = client.get()
                    .uri("/test")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            assertThat(body).contains("ok");

            assertThat(server.takeRequest().getPath()).isEqualTo("/test");
        }
    }

}




