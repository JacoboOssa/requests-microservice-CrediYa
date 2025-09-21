package co.com.crediya.consumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "adapter.restconsumer.paths")
public class RestConsumerPath {
    private String findByIdentificationNumber;
    private String validateToken;
    private String getAllUserInfoByEmail;

}
