package co.com.crediya.consumer.dto;

public record AuthUserResponseDTO(
        String email,
        String rol,
        String name,
        String lastName,
        Double baseSalary
) {
}
