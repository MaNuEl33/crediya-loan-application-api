package co.com.crediya.consumer.mappers;

import co.com.crediya.consumer.dtos.UserDto;
import co.com.crediya.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {
    User toUserModel(UserDto userDto);
}
