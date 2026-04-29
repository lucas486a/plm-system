package com.plm.service;

import com.plm.dto.ComponentDraftDTO;
import com.plm.dto.ECOApprovalDTO;
import com.plm.dto.ECODTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ECOService {

    // CRUD operations
    ECODTO createECO(ECODTO ecoDTO);

    ECODTO getECOById(Long id);

    ECODTO getECOByNumber(String ecoNumber);

    ECODTO updateECO(Long id, ECODTO ecoDTO);

    void deleteECO(Long id);

    Page<ECODTO> listECOs(Pageable pageable);

    // Workflow operations
    ECODTO submitECO(Long id);

    ECOApprovalDTO approveECO(Long ecoId, Long approverId, String stage, String comments);

    ECOApprovalDTO rejectECO(Long ecoId, Long approverId, String stage, String comments);

    ECODTO applyECO(Long id);

    ECODTO closeECO(Long id);

    // Component drafts
    ComponentDraftDTO addComponentDraft(Long ecoId, ComponentDraftDTO draftDTO);

    List<ComponentDraftDTO> listComponentDrafts(Long ecoId);

    // ECR to ECO conversion
    ECODTO convertECRToECO(Long ecrId);
}
