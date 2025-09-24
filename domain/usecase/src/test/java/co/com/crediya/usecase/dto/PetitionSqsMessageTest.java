package co.com.crediya.usecase.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PetitionSqsMessageTest {

    @Test
    void mustConvertToJsonCorrectly() {
        PetitionSqsMessage message = PetitionSqsMessage.builder()
                .petitionId("123")
                .email("test@email.com")
                .statusName("APROBADA")
                .statusDescription("Solicitud aprobada")
                .loanTypeName("Personal Loan")
                .amount(BigDecimal.valueOf(5000000))
                .term(24)
                .build();

        String json = message.toJson();

        // Validamos la estructura JSON
        assertThat(json).isEqualTo("{"
                + "\"petitionId\":\"123\","
                + "\"email\":\"test@email.com\","
                + "\"statusName\":\"APROBADA\","
                + "\"statusDescription\":\"Solicitud aprobada\","
                + "\"loanTypeName\":\"Personal Loan\","
                + "\"amount\":5000000,"
                + "\"term\":24"
                + "}");

        // Validaciones más flexibles (por si cambia orden en el futuro)
        assertThat(json).contains("\"petitionId\":\"123\"");
        assertThat(json).contains("\"email\":\"test@email.com\"");
        assertThat(json).contains("\"statusName\":\"APROBADA\"");
        assertThat(json).contains("\"amount\":5000000");
        assertThat(json).contains("\"term\":24");
    }
}
