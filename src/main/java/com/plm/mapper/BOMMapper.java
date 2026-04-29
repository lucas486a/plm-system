package com.plm.mapper;

import com.plm.dto.BOMDTO;
import com.plm.entity.BOM;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BOMMapper {

    @Mapping(source = "assembly.id", target = "assemblyId")
    @Mapping(source = "assembly.partNumber", target = "assemblyPartNumber")
    BOMDTO toDTO(BOM bom);

    @Mapping(source = "assemblyId", target = "assembly.id")
    @Mapping(source = "assemblyPartNumber", target = "assembly.partNumber")
    BOM toEntity(BOMDTO bomDTO);
}
