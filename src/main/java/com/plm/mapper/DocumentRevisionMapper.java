package com.plm.mapper;

import com.plm.dto.DocumentRevisionDTO;
import com.plm.entity.DocumentRevision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentRevisionMapper {

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "part.id", target = "partId")
    @Mapping(source = "assembly.id", target = "assemblyId")
    DocumentRevisionDTO toDto(DocumentRevision entity);

    @Mapping(target = "document", ignore = true)
    @Mapping(target = "part", ignore = true)
    @Mapping(target = "assembly", ignore = true)
    DocumentRevision toEntity(DocumentRevisionDTO dto);

    List<DocumentRevisionDTO> toDtoList(List<DocumentRevision> entities);

    List<DocumentRevision> toEntityList(List<DocumentRevisionDTO> dtos);
}
