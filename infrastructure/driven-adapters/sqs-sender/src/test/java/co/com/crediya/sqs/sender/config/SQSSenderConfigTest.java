package co.com.crediya.sqs.sender.config;


import co.com.crediya.sqs.sender.SQSSenderConfig;
import co.com.crediya.sqs.sender.automaticreview.config.SQSSenderProperties;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SQSSenderConfigTest {

    @Test
    void mustCreateSqsAsyncClient() {
        // Arrange
        SQSSenderProperties properties = mock(SQSSenderProperties.class);
        when(properties.region()).thenReturn("us-east-1");

        MetricPublisher publisher = mock(MetricPublisher.class);

        SQSSenderConfig config = new SQSSenderConfig();

        // Act
        SqsAsyncClient client = config.configSqs(properties, publisher);

        // Assert
        assertThat(client).isNotNull();
        assertThat(client.serviceClientConfiguration().region())
                .isEqualTo(Region.of("us-east-1"));
    }
}

