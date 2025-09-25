package co.com.crediya.config;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecretsPropertySourceConfig {

    private final GenericManagerAsync secretsManager;
    private final String requestsSecretName;
    private final String authConnectionSecretName;

    public SecretsPropertySourceConfig(GenericManagerAsync secretsManager,
                                       @Value("${aws.secrets.requests}") String requestsSecretName,
                                       @Value("${aws.secrets.authConnection}") String authConnectionSecretName) {
        this.secretsManager = secretsManager;
        this.requestsSecretName = requestsSecretName;
        this.authConnectionSecretName = authConnectionSecretName;
    }

    @Bean
    public PropertySource<?> awsSecretsPropertySource() throws SecretException {
        // Leemos el secret de requests
        Map<String, Object> requestsSecrets = secretsManager
                .getSecret(requestsSecretName, Map.class)
                .block();

        // Leemos el secret de auth_connection
        Map<String, Object> authSecrets = secretsManager
                .getSecret(authConnectionSecretName, Map.class)
                .block();

        // Combinamos ambos secrets
        Map<String, Object> combined = new HashMap<>();
        if (requestsSecrets != null) combined.putAll(requestsSecrets);
        if (authSecrets != null) combined.putAll(authSecrets);

        return new MapPropertySource("aws-secrets-combined", combined);
    }
}


