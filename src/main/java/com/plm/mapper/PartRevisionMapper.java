package com.plm.mapper;

import com.plm.dto.PartRevisionDTO;
import com.plm.entity.PartRevision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartRevisionMapper {

    @Mapping(source = "part.id", target = "partId")
    PartRevisionDTO toDto(PartRevision entity);

    @Mapping(target = "part", ignore = true)
    PartRevision toEntity(PartRevisionDTO dto);

    List<PartRevisionDTO> toDtoList(List<PartRevision> entities);

    List<PartRevision> toEntityList(List<PartRevisionDTO> dtos);
}
