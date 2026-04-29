package com.plm.service;

import com.plm.dto.ECRDTO;
import com.plm.dto.PartDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ECRService {

    // CRUD operations
    ECRDTO createECR(ECRDTO ecrDTO);

    ECRDTO getECRById(Long id);

    ECRDTO getECRByNumber(String ecrNumber);

    ECRDTO updateECR(Long id, ECRDTO ecrDTO);

    void deleteECR(Long id);

    Page<ECRDTO> listECRs(Pageable pageable);

    // Status management (state machine: DRAFT -> SUBMITTED -> EVALUATED -> APPROVED/REJECTED)
    ECRDTO submitECR(Long id);

    ECRDTO evaluateECR(Long id);

    ECRDTO approveECR(Long id);

    ECRDTO rejectECR(Long id);

    // Assignment
    ECRDTO assignECR(Long id, Long userId);

    // Impact analysis (affected parts)
    List<PartDTO> getAffectedParts(Long ecrId);

    PartDTO addAffectedPart(Long ecrId, Long partId);

    void removeAffectedPart(Long ecrId, Long partId);
}
