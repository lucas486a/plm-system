package com.plm.service;

import com.plm.dto.PartDTO;
import com.plm.dto.PartRevisionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartService {

    PartDTO createPart(PartDTO partDTO);

    PartDTO getPartById(Long id);

    PartDTO getPartByNumber(String partNumber);

    PartDTO updatePart(Long id, PartDTO partDTO);

    void deletePart(Long id);

    Page<PartDTO> listParts(Pageable pageable);

    Page<PartDTO> searchParts(String query, Pageable pageable);

    PartRevisionDTO createPartRevision(Long partId, PartRevisionDTO revisionDTO);

    Page<PartRevisionDTO> listPartRevisions(Long partId, Pageable pageable);
}
