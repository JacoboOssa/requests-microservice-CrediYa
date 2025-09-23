package co.com.crediya.sqs.sender.reports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.reports")
public record SQSSenderProperties(
     String region,
     String queueUrl){
}
