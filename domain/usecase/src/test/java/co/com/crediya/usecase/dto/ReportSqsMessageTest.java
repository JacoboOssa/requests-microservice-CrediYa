package co.com.crediya.usecase.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ReportSqsMessageTest {

    @Test
    void mustConvertToJsonCorrectly() {
        ReportSqsMessage message = ReportSqsMessage.builder()
                .petitionId("ABC123")
                .email("report@test.com")
                .amount(BigDecimal.valueOf(2500000))
                .build();

        String json = message.toJson();

        // Validación exacta
        assertThat(json).isEqualTo("{"
                + "\"petitionId\":\"ABC123\","
                + "\"email\":\"report@test.com\","
                + "\"amount\":2500000"
                + "}");

        // Validaciones más flexibles
        assertThat(json).contains("\"petitionId\":\"ABC123\"");
        assertThat(json).contains("\"email\":\"report@test.com\"");
        assertThat(json).contains("\"amount\":2500000");
    }
}
