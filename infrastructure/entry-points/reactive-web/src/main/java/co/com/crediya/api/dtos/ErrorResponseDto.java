package co.com.crediya.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorResponseDto(
        @Schema(description = "HTTP status code.", example = "400")
        Integer status,
        @Schema(description = "Error message.", example = "Field required.")
        String message) {
}
