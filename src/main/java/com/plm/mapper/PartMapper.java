package com.plm.mapper;

import com.plm.dto.PartDTO;
import com.plm.entity.Part;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartMapper {

    PartDTO toDto(Part entity);

    @Mapping(target = "revisions", ignore = true)
    Part toEntity(PartDTO dto);

    List<PartDTO> toDtoList(List<Part> entities);

    List<Part> toEntityList(List<PartDTO> dtos);
}
