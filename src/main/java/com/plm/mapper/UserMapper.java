package com.plm.mapper;

import com.plm.dto.UserDTO;
import com.plm.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDTO toDto(User entity);

    User toEntity(UserDTO dto);

    List<UserDTO> toDtoList(List<User> entities);

    List<User> toEntityList(List<UserDTO> dtos);
}
