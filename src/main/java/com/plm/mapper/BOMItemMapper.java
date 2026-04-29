package com.plm.mapper;

import com.plm.dto.BOMItemDTO;
import com.plm.entity.BOMItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BOMItemMapper {

    @Mapping(source = "bom.id", target = "bomId")
    @Mapping(source = "partRevision.id", target = "partRevisionId")
    @Mapping(source = "partRevision.revision", target = "partRevisionRevision")
    BOMItemDTO toDTO(BOMItem bomItem);

    @Mapping(source = "bomId", target = "bom.id")
    @Mapping(source = "partRevisionId", target = "partRevision.id")
    @Mapping(source = "partRevisionRevision", target = "partRevision.revision")
    BOMItem toEntity(BOMItemDTO bomItemDTO);
}
