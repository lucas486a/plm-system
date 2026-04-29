package com.plm.mapper;

import com.plm.dto.ECRDTO;
import com.plm.entity.ECR;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ECRMapper {

    @Mapping(source = "assignedTo.id", target = "assignedToId")
    ECRDTO toDTO(ECR ecr);

    @Mapping(source = "assignedToId", target = "assignedTo.id")
    ECR toEntity(ECRDTO dto);
}
