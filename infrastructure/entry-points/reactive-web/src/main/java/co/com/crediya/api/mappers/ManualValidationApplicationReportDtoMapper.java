package co.com.crediya.api.mappers;

import co.com.crediya.api.dtos.ManualValidationApplicationReportRequestDto;
import co.com.crediya.api.dtos.ManualValidationApplicationReportResponseDto;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReport;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ManualValidationApplicationReportDtoMapper {

    ManualValidationApplicationReportFilter toManualValidationApplicationReportFilter(
            ManualValidationApplicationReportRequestDto requestDto);

    @Mapping(target = "isFirstPage", source = "firstPage")
    @Mapping(target = "isLastPage", source = "lastPage")
    @Mapping(target = "isEmpty", source = "empty")
    ManualValidationApplicationReportResponseDto toManualValidationApplicationReportResponseDto(
            ManualValidationApplicationReport report);
}
