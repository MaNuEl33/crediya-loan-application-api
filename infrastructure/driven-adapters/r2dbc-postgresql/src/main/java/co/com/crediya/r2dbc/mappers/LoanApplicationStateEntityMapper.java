package co.com.crediya.r2dbc.mappers;

import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.r2dbc.entities.LoanApplicationStateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanApplicationStateEntityMapper {
    LoanApplicationState toLoanApplicationState(LoanApplicationStateEntity entity);
}
