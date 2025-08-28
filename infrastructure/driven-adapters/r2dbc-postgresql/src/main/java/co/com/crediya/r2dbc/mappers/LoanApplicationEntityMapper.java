package co.com.crediya.r2dbc.mappers;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.r2dbc.entities.LoanApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanApplicationEntityMapper {

    @Mapping(target = "stateId", source = "state.id")
    @Mapping(target = "loanTypeId", source = "loanType.id")
    LoanApplicationEntity toLoadApplicationEntity(LoanApplication loanApplication);

    @Mapping(target = "state", source = "stateId")
    @Mapping(target = "loanType", source = "loanTypeId")
    LoanApplication toLoadApplicationModel(LoanApplicationEntity loanApplicationEntity);

    @Mapping(target = "id", source = "stateId")
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "description", ignore = true)
    LoanApplicationState toLoanApplicationState(Long stateId);

    @Mapping(target = "id", source = "loanTypeId")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "minimumAmount", ignore = true)
    @Mapping(target = "maximumAmount", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "isAutoValidationEnabled", ignore = true)
    LoanType toLoanType(Long loanTypeId);
}
