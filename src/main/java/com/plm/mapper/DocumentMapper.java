package com.plm.mapper;

import com.plm.dto.DocumentDTO;
import com.plm.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    DocumentDTO toDto(Document entity);

    @Mapping(target = "revisions", ignore = true)
    Document toEntity(DocumentDTO dto);

    List<DocumentDTO> toDtoList(List<Document> entities);

    List<Document> toEntityList(List<DocumentDTO> dtos);
}
