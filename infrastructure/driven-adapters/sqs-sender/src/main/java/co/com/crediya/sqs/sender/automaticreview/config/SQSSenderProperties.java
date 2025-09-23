package co.com.crediya.sqs.sender.automaticreview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.automatic-review")
public record SQSSenderProperties(
     String region,
     String queueUrl){
}
