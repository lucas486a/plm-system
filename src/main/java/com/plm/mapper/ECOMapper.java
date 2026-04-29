package com.plm.mapper;

import com.plm.dto.ECODTO;
import com.plm.entity.ECO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ECOMapper {

    @Mapping(source = "ecr.id", target = "ecrId")
    ECODTO toDTO(ECO eco);

    @Mapping(source = "ecrId", target = "ecr.id")
    ECO toEntity(ECODTO dto);
}
