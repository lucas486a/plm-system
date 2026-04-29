package com.plm.service;

import com.plm.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BOMService {

    // BOM CRUD
    BOMDTO createBOM(BOMDTO bomDTO);

    BOMDTO getBOMById(Long id);

    BOMDTO updateBOM(Long id, BOMDTO bomDTO);

    void deleteBOM(Long id);

    Page<BOMDTO> listBOMs(Pageable pageable);

    // BOM Item CRUD
    BOMItemDTO addBOMItem(Long bomId, BOMItemDTO itemDTO);

    List<BOMItemDTO> getBOMItems(Long bomId);

    BOMItemDTO updateBOMItem(Long bomId, Long itemId, BOMItemDTO itemDTO);

    void removeBOMItem(Long bomId, Long itemId);

    // Multi-level BOM expansion
    List<BOMExplodeNode> explodeBOM(Long bomId);

    // Where-Used analysis (reverse BOM lookup)
    List<BOMDTO> whereUsed(Long partRevisionId);

    // BOM copy
    BOMDTO copyBOM(Long bomId, String newName);

    // BOM Comparison
    BOMComparisonResult compareBOMs(Long bom1Id, Long bom2Id);

    // BOM Export/Import
    String exportBOMToCsv(Long bomId);

    BOMDTO importBOMFromCsv(String csvData, Long assemblyId, String bomName);

    // BOM Snapshots (versioning)
    BOMSnapshot createSnapshot(Long bomId, String label);

    List<BOMSnapshot> listSnapshots(Long bomId);

    // BOM Cost Calculation
    BigDecimal calculateBOMCost(Long bomId);
}
