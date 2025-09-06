package co.com.crediya.api.handlers;

import co.com.crediya.api.dtos.ManualValidationApplicationReportRequestDto;
import co.com.crediya.api.helpers.ValidatorHelper;
import co.com.crediya.api.mappers.ManualValidationApplicationReportDtoMapper;
import co.com.crediya.usecase.manualvalidationapplicationreport.ManualValidationApplicationReportUseCase;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Component
@RequiredArgsConstructor
@Log4j2
public class ManualValidationApplicationReportHandler {
    private final ManualValidationApplicationReportUseCase useCase;
    private final ManualValidationApplicationReportDtoMapper dtoMapper;
    private final Validator validator;

    public Mono<ServerResponse> listenManualValidationApplicationReport(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ManualValidationApplicationReportRequestDto.class)
                .transform(requestBody -> ValidatorHelper.validateObject(requestBody, validator))
                .map(requestBody ->
                        Tuples.of(requestBody, this.dtoMapper.toManualValidationApplicationReportFilter(requestBody)))
                .flatMap(t ->
                        this.useCase.getManualValidationApplicationReport(t.getT2(), t.getT1().pageNumber(), t.getT1().pageSize()))
                .map(this.dtoMapper::toManualValidationApplicationReportResponseDto)
                .flatMap(r -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(r))
                .doFirst(() -> log.info("New request to retrieve a manual validation application report."))
                .doOnSuccess(r -> log.info("The manual validation application report was successfully retrieved."))
                .doOnError(err -> log.error("Error retrieving the manual validation application report", err));
    }
}
