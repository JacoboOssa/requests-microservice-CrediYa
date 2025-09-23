package co.com.crediya.sqs.sender.manualreview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.manual-review")
public record SQSSenderProperties(
     String region,
     String queueUrl){
}
