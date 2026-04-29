package com.plm.mapper;

import com.plm.dto.AssemblyDTO;
import com.plm.entity.Assembly;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssemblyMapper {

    AssemblyDTO toDTO(Assembly assembly);

    Assembly toEntity(AssemblyDTO assemblyDTO);
}
