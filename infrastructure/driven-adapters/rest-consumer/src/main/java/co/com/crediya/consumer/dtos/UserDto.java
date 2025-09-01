package co.com.crediya.consumer.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String address,
        String email,
        String phoneNumber,
        String documentNumber,
        BigDecimal baseSalary
) {
}
