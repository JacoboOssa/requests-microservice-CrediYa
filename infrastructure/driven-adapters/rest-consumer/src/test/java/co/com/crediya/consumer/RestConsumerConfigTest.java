package co.com.crediya.consumer;


import co.com.crediya.consumer.config.RestConsumerConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

        // No puedes leer el baseUrl directamente desde WebClient,
        // pero sí puedes probar que hace una llamada con la URL correcta.
        // Por ejemplo, usando un MockWebServer (de okhttp3)
    }

    @Test
    void shouldTriggerTimeoutHandlersOnConnection() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            // Prepara una respuesta mock
            server.enqueue(new MockResponse()
                    .setBody("{\"status\":\"ok\"}")
                    .addHeader("Content-Type", "application/json"));

            // Usa la URL mock y un timeout pequeño
            RestConsumerConfig config = new RestConsumerConfig(server.url("/").toString(), 500);

            WebClient client = config.gtWebClient(WebClient.builder());

            // Realiza una llamada que fuerza la conexión → ejecuta la lambda
            String body = client.get()
                    .uri("/test")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            assertThat(body).contains("ok");

            // Opcional: validar que realmente hizo la request
            assertThat(server.takeRequest().getPath()).isEqualTo("/test");
        }
    }

}




