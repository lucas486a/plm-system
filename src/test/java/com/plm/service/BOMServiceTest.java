package com.plm.service;

import com.plm.dto.*;
import com.plm.entity.*;
import com.plm.mapper.BOMItemMapper;
import com.plm.mapper.BOMMapper;
import com.plm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BOMServiceTest {

    @Mock
    private BOMRepository bomRepository;
    @Mock
    private BOMItemRepository bomItemRepository;
    @Mock
    private AssemblyRepository assemblyRepository;
    @Mock
    private PartRevisionRepository partRevisionRepository;
    @Mock
    private PartRepository partRepository;
    @Mock
    private BOMMapper bomMapper;
    @Mock
    private BOMItemMapper bomItemMapper;

    @InjectMocks
    private BOMServiceImpl bomService;

    private Assembly assembly;
    private BOM bom;
    private BOMDTO bomDTO;
    private Part part;
    private PartRevision partRevision;
    private BOMItem bomItem;
    private BOMItemDTO bomItemDTO;

    @BeforeEach
    void setUp() {
        assembly = Assembly.builder()
                .id(1L)
                .partNumber("ASM-001")
                .name("Test Assembly")
                .build();

        bom = BOM.builder()
                .id(1L)
                .name("Test BOM")
                .assembly(assembly)
                .status("DRAFT")
                .versionNumber(1)
                .build();

        bomDTO = BOMDTO.builder()
                .id(1L)
                .name("Test BOM")
                .assemblyId(1L)
                .assemblyPartNumber("ASM-001")
                .status("DRAFT")
                .versionNumber(1)
                .build();

        part = Part.builder()
                .id(1L)
                .partNumber("P-001")
                .name("Test Part")
                .build();

        partRevision = PartRevision.builder()
                .id(1L)
                .part(part)
                .revision("A")
                .iteration(1)
                .price(new BigDecimal("10.00"))
                .build();

        bomItem = BOMItem.builder()
                .id(1L)
                .bom(bom)
                .partRevision(partRevision)
                .quantity(new BigDecimal("5"))
                .designator("R1")
                .findNumber(1)
                .isMounted(true)
                .scrapFactor(BigDecimal.ZERO)
                .build();

        bomItemDTO = BOMItemDTO.builder()
                .id(1L)
                .bomId(1L)
                .partRevisionId(1L)
                .quantity(new BigDecimal("5"))
                .designator("R1")
                .findNumber(1)
                .isMounted(true)
                .scrapFactor(BigDecimal.ZERO)
                .build();
    }

    // ==================== createBOM ====================

    @Nested
    @DisplayName("createBOM")
    class CreateBOM {

        @Test
        @DisplayName("should create BOM successfully")
        void createBOM_success() {
            when(assemblyRepository.findById(1L)).thenReturn(Optional.of(assembly));
            when(bomMapper.toEntity(any(BOMDTO.class))).thenReturn(bom);
            when(bomRepository.save(any(BOM.class))).thenReturn(bom);
            when(bomMapper.toDTO(any(BOM.class))).thenReturn(bomDTO);

            BOMDTO result = bomService.createBOM(bomDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test BOM");
            verify(bomRepository).save(any(BOM.class));
        }

        @Test
        @DisplayName("should throw when assembly not found")
        void createBOM_assemblyNotFound() {
            bomDTO.setAssemblyId(99L);
            when(assemblyRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.createBOM(bomDTO))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("should default status to DRAFT when null")
        void createBOM_defaultStatus() {
            bomDTO.setStatus(null);
            when(assemblyRepository.findById(1L)).thenReturn(Optional.of(assembly));
            when(bomMapper.toEntity(any(BOMDTO.class))).thenReturn(bom);
            when(bomRepository.save(any(BOM.class))).thenReturn(bom);
            when(bomMapper.toDTO(any(BOM.class))).thenReturn(bomDTO);

            bomService.createBOM(bomDTO);

            assertThat(bom.getStatus()).isEqualTo("DRAFT");
        }
    }

    // ==================== getBOMById ====================

    @Nested
    @DisplayName("getBOMById")
    class GetBOMById {

        @Test
        @DisplayName("should return BOM when found")
        void getBOMById_found() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomMapper.toDTO(bom)).thenReturn(bomDTO);

            BOMDTO result = bomService.getBOMById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void getBOMById_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.getBOMById(99L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== updateBOM ====================

    @Nested
    @DisplayName("updateBOM")
    class UpdateBOM {

        @Test
        @DisplayName("should update BOM successfully")
        void updateBOM_success() {
            BOMDTO updateDTO = BOMDTO.builder()
                    .name("Updated BOM")
                    .status("ACTIVE")
                    .comments("Updated comments")
                    .versionNumber(2)
                    .build();

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomRepository.save(any(BOM.class))).thenReturn(bom);
            when(bomMapper.toDTO(any(BOM.class))).thenReturn(updateDTO);

            BOMDTO result = bomService.updateBOM(1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated BOM");
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void updateBOM_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.updateBOM(99L, bomDTO))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("should only update non-null fields")
        void updateBOM_partialUpdate() {
            BOMDTO partialDTO = BOMDTO.builder().name("New Name").build();

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomRepository.save(any(BOM.class))).thenReturn(bom);
            when(bomMapper.toDTO(any(BOM.class))).thenReturn(partialDTO);

            bomService.updateBOM(1L, partialDTO);

            assertThat(bom.getName()).isEqualTo("New Name");
            assertThat(bom.getStatus()).isEqualTo("DRAFT"); // unchanged
        }
    }

    // ==================== deleteBOM ====================

    @Nested
    @DisplayName("deleteBOM")
    class DeleteBOM {

        @Test
        @DisplayName("should delete BOM successfully")
        void deleteBOM_success() {
            when(bomRepository.existsById(1L)).thenReturn(true);
            doNothing().when(bomRepository).deleteById(1L);

            bomService.deleteBOM(1L);

            verify(bomRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void deleteBOM_notFound() {
            when(bomRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> bomService.deleteBOM(99L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== listBOMs ====================

    @Nested
    @DisplayName("listBOMs")
    class ListBOMs {

        @Test
        @DisplayName("should return paginated BOMs")
        void listBOMs_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BOM> page = new PageImpl<>(List.of(bom), pageable, 1);
            when(bomRepository.findAll(pageable)).thenReturn(page);
            when(bomMapper.toDTO(bom)).thenReturn(bomDTO);

            Page<BOMDTO> result = bomService.listBOMs(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== addBOMItem ====================

    @Nested
    @DisplayName("addBOMItem")
    class AddBOMItem {

        @Test
        @DisplayName("should add BOM item successfully")
        void addBOMItem_success() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(partRevisionRepository.findById(1L)).thenReturn(Optional.of(partRevision));
            when(assemblyRepository.findByPartNumber("P-001")).thenReturn(Optional.empty());
            when(bomItemMapper.toEntity(any(BOMItemDTO.class))).thenReturn(bomItem);
            when(bomItemRepository.save(any(BOMItem.class))).thenReturn(bomItem);
            when(bomItemMapper.toDTO(any(BOMItem.class))).thenReturn(bomItemDTO);

            BOMItemDTO result = bomService.addBOMItem(1L, bomItemDTO);

            assertThat(result).isNotNull();
            verify(bomItemRepository).save(any(BOMItem.class));
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void addBOMItem_bomNotFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.addBOMItem(99L, bomItemDTO))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("should throw when part revision not found")
        void addBOMItem_partRevisionNotFound() {
            bomItemDTO.setPartRevisionId(99L);
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(partRevisionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.addBOMItem(1L, bomItemDTO))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== getBOMItems ====================

    @Nested
    @DisplayName("getBOMItems")
    class GetBOMItems {

        @Test
        @DisplayName("should return BOM items")
        void getBOMItems_success() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));
            when(bomItemMapper.toDTO(bomItem)).thenReturn(bomItemDTO);

            List<BOMItemDTO> result = bomService.getBOMItems(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no items")
        void getBOMItems_empty() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(Collections.emptyList());

            List<BOMItemDTO> result = bomService.getBOMItems(1L);

            assertThat(result).isEmpty();
        }
    }

    // ==================== updateBOMItem ====================

    @Nested
    @DisplayName("updateBOMItem")
    class UpdateBOMItem {

        @Test
        @DisplayName("should update BOM item successfully")
        void updateBOMItem_success() {
            BOMItemDTO updateDTO = BOMItemDTO.builder()
                    .quantity(new BigDecimal("10"))
                    .designator("R2")
                    .findNumber(2)
                    .build();

            when(bomItemRepository.findById(1L)).thenReturn(Optional.of(bomItem));
            when(bomItemRepository.save(any(BOMItem.class))).thenReturn(bomItem);
            when(bomItemMapper.toDTO(any(BOMItem.class))).thenReturn(updateDTO);

            BOMItemDTO result = bomService.updateBOMItem(1L, 1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getQuantity()).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("should throw when BOM item not found")
        void updateBOMItem_notFound() {
            when(bomItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.updateBOMItem(1L, 99L, bomItemDTO))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("should throw when item does not belong to BOM")
        void updateBOMItem_wrongBOM() {
            BOMItem otherItem = BOMItem.builder()
                    .id(1L)
                    .bom(BOM.builder().id(999L).build())
                    .build();

            when(bomItemRepository.findById(1L)).thenReturn(Optional.of(otherItem));

            assertThatThrownBy(() -> bomService.updateBOMItem(1L, 1L, bomItemDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("does not belong");
        }
    }

    // ==================== removeBOMItem ====================

    @Nested
    @DisplayName("removeBOMItem")
    class RemoveBOMItem {

        @Test
        @DisplayName("should remove BOM item successfully")
        void removeBOMItem_success() {
            when(bomItemRepository.findById(1L)).thenReturn(Optional.of(bomItem));
            doNothing().when(bomItemRepository).deleteById(1L);

            bomService.removeBOMItem(1L, 1L);

            verify(bomItemRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when BOM item not found")
        void removeBOMItem_notFound() {
            when(bomItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.removeBOMItem(1L, 99L))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("should throw when item does not belong to BOM")
        void removeBOMItem_wrongBOM() {
            BOMItem otherItem = BOMItem.builder()
                    .id(1L)
                    .bom(BOM.builder().id(999L).build())
                    .build();

            when(bomItemRepository.findById(1L)).thenReturn(Optional.of(otherItem));

            assertThatThrownBy(() -> bomService.removeBOMItem(1L, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("does not belong");
        }
    }

    // ==================== explodeBOM ====================

    @Nested
    @DisplayName("explodeBOM")
    class ExplodeBOM {

        @Test
        @DisplayName("should explode BOM with single level")
        void explodeBOM_singleLevel() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));
            lenient().when(assemblyRepository.findByPartNumber("P-001")).thenReturn(Optional.empty());

            List<BOMExplodeNode> result = bomService.explodeBOM(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPartNumber()).isEqualTo("P-001");
            assertThat(result.get(0).getLevel()).isEqualTo(0);
            assertThat(result.get(0).getChildren()).isEmpty();
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void explodeBOM_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.explodeBOM(99L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== whereUsed ====================

    @Nested
    @DisplayName("whereUsed")
    class WhereUsed {

        @Test
        @DisplayName("should return BOMs containing the part revision")
        void whereUsed_success() {
            when(partRevisionRepository.existsById(1L)).thenReturn(true);
            when(bomItemRepository.findByPartRevisionId(1L)).thenReturn(List.of(bomItem));
            when(bomMapper.toDTO(bom)).thenReturn(bomDTO);

            List<BOMDTO> result = bomService.whereUsed(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw when part revision not found")
        void whereUsed_notFound() {
            when(partRevisionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> bomService.whereUsed(99L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== copyBOM ====================

    @Nested
    @DisplayName("copyBOM")
    class CopyBOM {

        @Test
        @DisplayName("should copy BOM with items")
        void copyBOM_success() {
            BOM copiedBom = BOM.builder()
                    .id(2L)
                    .name("Copied BOM")
                    .assembly(assembly)
                    .status("DRAFT")
                    .versionNumber(1)
                    .build();

            BOMDTO copiedDTO = BOMDTO.builder()
                    .id(2L)
                    .name("Copied BOM")
                    .assemblyId(1L)
                    .status("DRAFT")
                    .versionNumber(1)
                    .build();

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomRepository.save(any(BOM.class))).thenReturn(copiedBom);
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));
            when(bomItemRepository.save(any(BOMItem.class))).thenReturn(bomItem);
            when(bomMapper.toDTO(copiedBom)).thenReturn(copiedDTO);

            BOMDTO result = bomService.copyBOM(1L, "Copied BOM");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Copied BOM");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            verify(bomItemRepository).save(any(BOMItem.class));
        }

        @Test
        @DisplayName("should throw when source BOM not found")
        void copyBOM_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.copyBOM(99L, "Copy"))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== compareBOMs ====================

    @Nested
    @DisplayName("compareBOMs")
    class CompareBOMs {

        @Test
        @DisplayName("should detect added items")
        void compareBOMs_addedItems() {
            BOM bom2 = BOM.builder().id(2L).name("BOM 2").assembly(assembly).build();
            BOMDTO bom2DTO = BOMDTO.builder().id(2L).name("BOM 2").build();

            PartRevision pr2 = PartRevision.builder()
                    .id(2L)
                    .part(Part.builder().id(2L).partNumber("P-002").name("Part 2").build())
                    .revision("A")
                    .build();

            BOMItem item2 = BOMItem.builder()
                    .id(2L).bom(bom2).partRevision(pr2)
                    .quantity(new BigDecimal("3")).build();

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomRepository.findById(2L)).thenReturn(Optional.of(bom2));
            when(bomItemRepository.findByBomId(1L)).thenReturn(Collections.emptyList());
            when(bomItemRepository.findByBomId(2L)).thenReturn(List.of(item2));

            BOMComparisonResult result = bomService.compareBOMs(1L, 2L);

            assertThat(result.getAddedItems()).hasSize(1);
            assertThat(result.getRemovedItems()).isEmpty();
            assertThat(result.getModifiedItems()).isEmpty();
        }

        @Test
        @DisplayName("should detect removed items")
        void compareBOMs_removedItems() {
            BOM bom2 = BOM.builder().id(2L).name("BOM 2").assembly(assembly).build();

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomRepository.findById(2L)).thenReturn(Optional.of(bom2));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));
            when(bomItemRepository.findByBomId(2L)).thenReturn(Collections.emptyList());

            BOMComparisonResult result = bomService.compareBOMs(1L, 2L);

            assertThat(result.getAddedItems()).isEmpty();
            assertThat(result.getRemovedItems()).hasSize(1);
        }

        @Test
        @DisplayName("should detect modified items")
        void compareBOMs_modifiedItems() {
            BOM bom2 = BOM.builder().id(2L).name("BOM 2").assembly(assembly).build();

            BOMItem item2 = BOMItem.builder()
                    .id(2L).bom(bom2).partRevision(partRevision)
                    .quantity(new BigDecimal("10")) // different quantity
                    .designator("R1")
                    .findNumber(1)
                    .build();

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomRepository.findById(2L)).thenReturn(Optional.of(bom2));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));
            when(bomItemRepository.findByBomId(2L)).thenReturn(List.of(item2));

            BOMComparisonResult result = bomService.compareBOMs(1L, 2L);

            assertThat(result.getModifiedItems()).hasSize(1);
            assertThat(result.getModifiedItems().get(0).getBom1Quantity()).isEqualTo(new BigDecimal("5"));
            assertThat(result.getModifiedItems().get(0).getBom2Quantity()).isEqualTo(new BigDecimal("10"));
        }
    }

    // ==================== exportBOMToCsv ====================

    @Nested
    @DisplayName("exportBOMToCsv")
    class ExportBOMToCsv {

        @Test
        @DisplayName("should export BOM to CSV format")
        void exportBOMToCsv_success() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));

            String csv = bomService.exportBOMToCsv(1L);

            assertThat(csv).contains("partNumber,partName,revision");
            assertThat(csv).contains("P-001");
            assertThat(csv).contains("Test Part");
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void exportBOMToCsv_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.exportBOMToCsv(99L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== importBOMFromCsv ====================

    @Nested
    @DisplayName("importBOMFromCsv")
    class ImportBOMFromCsv {

        @Test
        @DisplayName("should import BOM from CSV")
        void importBOMFromCsv_success() {
            String csv = "partNumber,partName,revision,quantity,designator,findNumber,isMounted,scrapFactor,comment\n" +
                         "P-001,Test Part,A,5,R1,1,true,0,test comment\n";

            when(assemblyRepository.findById(1L)).thenReturn(Optional.of(assembly));
            when(bomRepository.save(any(BOM.class))).thenReturn(bom);
            when(partRepository.findByPartNumber("P-001")).thenReturn(Optional.of(part));
            when(partRevisionRepository.findByPartIdAndRevisionAndIteration(1L, "A", 1))
                    .thenReturn(Optional.of(partRevision));
            when(bomItemRepository.save(any(BOMItem.class))).thenReturn(bomItem);
            when(bomMapper.toDTO(any(BOM.class))).thenReturn(bomDTO);

            BOMDTO result = bomService.importBOMFromCsv(csv, 1L, "Imported BOM");

            assertThat(result).isNotNull();
            verify(bomItemRepository).save(any(BOMItem.class));
        }

        @Test
        @DisplayName("should throw when assembly not found")
        void importBOMFromCsv_assemblyNotFound() {
            when(assemblyRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.importBOMFromCsv("data", 99L, "BOM"))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        @DisplayName("should skip unknown parts")
        void importBOMFromCsv_skipUnknownParts() {
            String csv = "partNumber,partName,revision,quantity\n" +
                         "UNKNOWN,Unknown Part,A,1\n";

            when(assemblyRepository.findById(1L)).thenReturn(Optional.of(assembly));
            when(bomRepository.save(any(BOM.class))).thenReturn(bom);
            when(partRepository.findByPartNumber("UNKNOWN")).thenReturn(Optional.empty());
            when(bomMapper.toDTO(any(BOM.class))).thenReturn(bomDTO);

            BOMDTO result = bomService.importBOMFromCsv(csv, 1L, "BOM");

            assertThat(result).isNotNull();
            verify(bomItemRepository, never()).save(any());
        }
    }

    // ==================== createSnapshot ====================

    @Nested
    @DisplayName("createSnapshot")
    class CreateSnapshot {

        @Test
        @DisplayName("should create snapshot with items and cost")
        void createSnapshot_success() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));

            BOMSnapshot snapshot = bomService.createSnapshot(1L, "Test Snapshot");

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getBomId()).isEqualTo(1L);
            assertThat(snapshot.getSnapshotLabel()).isEqualTo("Test Snapshot");
            assertThat(snapshot.getItems()).hasSize(1);
            assertThat(snapshot.getTotalCost()).isEqualTo(new BigDecimal("50.0000"));
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void createSnapshot_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.createSnapshot(99L, "label"))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    // ==================== listSnapshots ====================

    @Nested
    @DisplayName("listSnapshots")
    class ListSnapshots {

        @Test
        @DisplayName("should return empty list when no snapshots")
        void listSnapshots_empty() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));

            List<BOMSnapshot> result = bomService.listSnapshots(1L);

            assertThat(result).isEmpty();
        }
    }

    // ==================== calculateBOMCost ====================

    @Nested
    @DisplayName("calculateBOMCost")
    class CalculateBOMCost {

        @Test
        @DisplayName("should calculate total cost correctly")
        void calculateBOMCost_success() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));

            BigDecimal cost = bomService.calculateBOMCost(1L);

            // 5 * 10.00 = 50.0000
            assertThat(cost).isEqualTo(new BigDecimal("50.0000"));
        }

        @Test
        @DisplayName("should include scrap factor in calculation")
        void calculateBOMCost_withScrap() {
            bomItem.setScrapFactor(new BigDecimal("10")); // 10% scrap

            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(List.of(bomItem));

            BigDecimal cost = bomService.calculateBOMCost(1L);

            // effective qty = 5 * (1 + 10/100) = 5.5
            // cost = 10.00 * 5.5 = 55.0000
            assertThat(cost).isEqualTo(new BigDecimal("55.0000"));
        }

        @Test
        @DisplayName("should return zero for empty BOM")
        void calculateBOMCost_empty() {
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(bomItemRepository.findByBomId(1L)).thenReturn(Collections.emptyList());

            BigDecimal cost = bomService.calculateBOMCost(1L);

            assertThat(cost).isEqualTo(new BigDecimal("0.0000"));
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void calculateBOMCost_notFound() {
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bomService.calculateBOMCost(99L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }
}
