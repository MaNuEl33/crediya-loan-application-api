package co.com.crediya.api.dtos;

import java.util.List;

public record ManualValidationApplicationReportResponseDto(
        List<ManualValidationApplicationReportResponseElementDto> applications,
        long totalApplications,
        long pageNumber,
        long pageSize,
        int totalPages,
        boolean isFirstPage,
        boolean isLastPage,
        boolean isEmpty
) {
}
