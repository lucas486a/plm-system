package com.plm.mapper;

import com.plm.dto.RoleDTO;
import com.plm.entity.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDTO toDto(Role entity);

    Role toEntity(RoleDTO dto);

    List<RoleDTO> toDtoList(List<Role> entities);

    List<Role> toEntityList(List<RoleDTO> dtos);
}
