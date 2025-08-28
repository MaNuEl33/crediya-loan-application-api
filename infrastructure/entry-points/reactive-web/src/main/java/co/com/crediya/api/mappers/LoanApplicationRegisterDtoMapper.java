package co.com.crediya.api.mappers;

import co.com.crediya.api.dtos.LoanApplicationRegisterRequestDto;
import co.com.crediya.api.dtos.LoanApplicationRegisterResponseDto;
import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loantype.LoanType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanApplicationRegisterDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "loanType", source = "loanTypeId")
    LoanApplication toLoanApplicationModel(LoanApplicationRegisterRequestDto requestDto);

    @Mapping(target = "id", source = "loanTypeId")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "minimumAmount", ignore = true)
    @Mapping(target = "maximumAmount", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "isAutoValidationEnabled", ignore = true)
    LoanType toLoanType(Long loanTypeId);

    LoanApplicationRegisterResponseDto toLoanApplicationRegisterResponseDto(LoanApplication loanApplication);
}
