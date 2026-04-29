package com.plm.mapper;

import com.plm.dto.ECOApprovalDTO;
import com.plm.entity.ECOApproval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ECOApprovalMapper {

    @Mapping(source = "eco.id", target = "ecoId")
    @Mapping(source = "approver.id", target = "approverId")
    ECOApprovalDTO toDTO(ECOApproval approval);

    @Mapping(source = "ecoId", target = "eco.id")
    @Mapping(source = "approverId", target = "approver.id")
    ECOApproval toEntity(ECOApprovalDTO dto);
}
