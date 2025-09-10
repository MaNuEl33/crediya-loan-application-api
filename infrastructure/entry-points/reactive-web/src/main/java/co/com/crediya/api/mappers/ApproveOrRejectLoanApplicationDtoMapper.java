package co.com.crediya.api.mappers;

import co.com.crediya.api.dtos.ApprovalStateDto;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApproveOrRejectLoanApplicationDtoMapper {
    LoanApplicationApprovalState toLoanApplicationApprovalState(ApprovalStateDto stateDto);
}
