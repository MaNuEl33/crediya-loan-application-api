package co.com.crediya.r2dbc.mappers;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.r2dbc.entities.LoanTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanTypeEntityMapper {
    LoanType toLoanType(LoanTypeEntity loanTypeEntity);
}
