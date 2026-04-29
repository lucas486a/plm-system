package com.plm.service;

import com.plm.dto.PartDTO;
import com.plm.dto.PartRevisionDTO;
import com.plm.entity.Part;
import com.plm.entity.PartRevision;
import com.plm.mapper.PartMapper;
import com.plm.mapper.PartRevisionMapper;
import com.plm.repository.PartRepository;
import com.plm.repository.PartRevisionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository partRepository;
    @Mock
    private PartRevisionRepository partRevisionRepository;
    @Mock
    private PartMapper partMapper;
    @Mock
    private PartRevisionMapper partRevisionMapper;

    @InjectMocks
    private PartServiceImpl partService;

    private Part part;
    private PartDTO partDTO;
    private PartRevision partRevision;
    private PartRevisionDTO partRevisionDTO;

    @BeforeEach
    void setUp() {
        part = Part.builder()
                .id(1L)
                .partNumber("P-001")
                .name("Test Part")
                .description("A test part")
                .partType("COMPONENT")
                .defaultUnit("EA")
                .build();

        partDTO = PartDTO.builder()
                .id(1L)
                .partNumber("P-001")
                .name("Test Part")
                .description("A test part")
                .partType("COMPONENT")
                .defaultUnit("EA")
                .build();

        partRevision = PartRevision.builder()
                .id(1L)
                .part(part)
                .revision("A")
                .iteration(1)
                .isLatestRevision(true)
                .build();

        partRevisionDTO = PartRevisionDTO.builder()
                .id(1L)
                .partId(1L)
                .revision("A")
                .iteration(1)
                .isLatestRevision(true)
                .build();
    }

    // ==================== createPart ====================

    @Nested
    @DisplayName("createPart")
    class CreatePart {

        @Test
        @DisplayName("should create part successfully")
        void createPart_success() {
            when(partRepository.existsByPartNumber("P-001")).thenReturn(false);
            when(partMapper.toEntity(any(PartDTO.class))).thenReturn(part);
            when(partRepository.save(any(Part.class))).thenReturn(part);
            when(partMapper.toDto(any(Part.class))).thenReturn(partDTO);

            PartDTO result = partService.createPart(partDTO);

            assertThat(result).isNotNull();
            assertThat(result.getPartNumber()).isEqualTo("P-001");
            verify(partRepository).save(part);
        }

        @Test
        @DisplayName("should throw when part number already exists")
        void createPart_duplicatePartNumber() {
            when(partRepository.existsByPartNumber("P-001")).thenReturn(true);

            assertThatThrownBy(() -> partService.createPart(partDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(partRepository, never()).save(any());
        }
    }

    // ==================== getPartById ====================

    @Nested
    @DisplayName("getPartById")
    class GetPartById {

        @Test
        @DisplayName("should return part when found")
        void getPartById_found() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partMapper.toDto(part)).thenReturn(partDTO);

            PartDTO result = partService.getPartById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when part not found")
        void getPartById_notFound() {
            when(partRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> partService.getPartById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Part not found");
        }
    }

    // ==================== getPartByNumber ====================

    @Nested
    @DisplayName("getPartByNumber")
    class GetPartByNumber {

        @Test
        @DisplayName("should return part when found by number")
        void getPartByNumber_found() {
            when(partRepository.findByPartNumber("P-001")).thenReturn(Optional.of(part));
            when(partMapper.toDto(part)).thenReturn(partDTO);

            PartDTO result = partService.getPartByNumber("P-001");

            assertThat(result).isNotNull();
            assertThat(result.getPartNumber()).isEqualTo("P-001");
        }

        @Test
        @DisplayName("should throw when part number not found")
        void getPartByNumber_notFound() {
            when(partRepository.findByPartNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> partService.getPartByNumber("UNKNOWN"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Part not found");
        }
    }

    // ==================== updatePart ====================

    @Nested
    @DisplayName("updatePart")
    class UpdatePart {

        @Test
        @DisplayName("should update part successfully")
        void updatePart_success() {
            PartDTO updateDTO = PartDTO.builder()
                    .partNumber("P-001")
                    .name("Updated Part")
                    .description("Updated description")
                    .partType("ASSEMBLY")
                    .defaultUnit("KG")
                    .build();

            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRepository.save(any(Part.class))).thenReturn(part);
            when(partMapper.toDto(any(Part.class))).thenReturn(updateDTO);

            PartDTO result = partService.updatePart(1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Part");
            verify(partRepository).save(part);
        }

        @Test
        @DisplayName("should throw when part not found")
        void updatePart_notFound() {
            when(partRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> partService.updatePart(99L, partDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when changing to duplicate part number")
        void updatePart_duplicatePartNumber() {
            PartDTO updateDTO = PartDTO.builder()
                    .partNumber("P-002")
                    .name("Updated")
                    .build();

            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRepository.existsByPartNumber("P-002")).thenReturn(true);

            assertThatThrownBy(() -> partService.updatePart(1L, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should allow update with same part number")
        void updatePart_samePartNumber() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRepository.save(any(Part.class))).thenReturn(part);
            when(partMapper.toDto(any(Part.class))).thenReturn(partDTO);

            PartDTO result = partService.updatePart(1L, partDTO);

            assertThat(result).isNotNull();
            verify(partRepository, never()).existsByPartNumber(anyString());
        }

        @Test
        @DisplayName("should throw OptimisticLockException on save conflict")
        void updatePart_optimisticLock() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRepository.save(any(Part.class))).thenThrow(new OptimisticLockException("conflict"));

            assertThatThrownBy(() -> partService.updatePart(1L, partDTO))
                    .isInstanceOf(OptimisticLockException.class);
        }
    }

    // ==================== deletePart ====================

    @Nested
    @DisplayName("deletePart")
    class DeletePart {

        @Test
        @DisplayName("should delete part successfully")
        void deletePart_success() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            doNothing().when(partRepository).delete(part);

            partService.deletePart(1L);

            verify(partRepository).delete(part);
        }

        @Test
        @DisplayName("should throw when part not found")
        void deletePart_notFound() {
            when(partRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> partService.deletePart(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw OptimisticLockException on delete conflict")
        void deletePart_optimisticLock() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            doThrow(new OptimisticLockException("conflict")).when(partRepository).delete(part);

            assertThatThrownBy(() -> partService.deletePart(1L))
                    .isInstanceOf(OptimisticLockException.class);
        }
    }

    // ==================== listParts ====================

    @Nested
    @DisplayName("listParts")
    class ListParts {

        @Test
        @DisplayName("should return paginated parts")
        void listParts_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Part> page = new PageImpl<>(List.of(part), pageable, 1);
            when(partRepository.findAll(pageable)).thenReturn(page);
            when(partMapper.toDto(part)).thenReturn(partDTO);

            Page<PartDTO> result = partService.listParts(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getPartNumber()).isEqualTo("P-001");
        }

        @Test
        @DisplayName("should return empty page when no parts")
        void listParts_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Part> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(partRepository.findAll(pageable)).thenReturn(page);

            Page<PartDTO> result = partService.listParts(pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ==================== searchParts ====================

    @Nested
    @DisplayName("searchParts")
    class SearchParts {

        @Test
        @DisplayName("should return matching parts")
        void searchParts_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Part> page = new PageImpl<>(List.of(part), pageable, 1);
            when(partRepository.searchParts("test", pageable)).thenReturn(page);
            when(partMapper.toDto(part)).thenReturn(partDTO);

            Page<PartDTO> result = partService.searchParts("test", pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== createPartRevision ====================

    @Nested
    @DisplayName("createPartRevision")
    class CreatePartRevision {

        @Test
        @DisplayName("should create revision successfully")
        void createRevision_success() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRevisionRepository.findByPartIdAndRevisionAndIteration(1L, "A", 1))
                    .thenReturn(Optional.empty());
            when(partRevisionRepository.findByPartIdAndIsLatestRevisionTrue(1L))
                    .thenReturn(Optional.empty());
            when(partRevisionMapper.toEntity(any(PartRevisionDTO.class))).thenReturn(partRevision);
            when(partRevisionRepository.save(any(PartRevision.class))).thenReturn(partRevision);
            when(partRevisionMapper.toDto(any(PartRevision.class))).thenReturn(partRevisionDTO);

            PartRevisionDTO result = partService.createPartRevision(1L, partRevisionDTO);

            assertThat(result).isNotNull();
            assertThat(result.getRevision()).isEqualTo("A");
            verify(partRevisionRepository).save(any(PartRevision.class));
        }

        @Test
        @DisplayName("should throw when part not found")
        void createRevision_partNotFound() {
            when(partRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> partService.createPartRevision(99L, partRevisionDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when revision already exists")
        void createRevision_duplicateRevision() {
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRevisionRepository.findByPartIdAndRevisionAndIteration(1L, "A", 1))
                    .thenReturn(Optional.of(partRevision));

            assertThatThrownBy(() -> partService.createPartRevision(1L, partRevisionDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should unmark previous latest revision")
        void createRevision_unmarksPreviousLatest() {
            PartRevision previousLatest = PartRevision.builder()
                    .id(2L).part(part).revision("A").iteration(1).isLatestRevision(true).build();

            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(partRevisionRepository.findByPartIdAndRevisionAndIteration(1L, "A", 1))
                    .thenReturn(Optional.empty());
            when(partRevisionRepository.findByPartIdAndIsLatestRevisionTrue(1L))
                    .thenReturn(Optional.of(previousLatest));
            when(partRevisionMapper.toEntity(any(PartRevisionDTO.class))).thenReturn(partRevision);
            when(partRevisionRepository.save(any(PartRevision.class))).thenReturn(partRevision);
            when(partRevisionMapper.toDto(any(PartRevision.class))).thenReturn(partRevisionDTO);

            partService.createPartRevision(1L, partRevisionDTO);

            assertThat(previousLatest.getIsLatestRevision()).isFalse();
            verify(partRevisionRepository).save(previousLatest);
        }
    }

    // ==================== listPartRevisions ====================

    @Nested
    @DisplayName("listPartRevisions")
    class ListPartRevisions {

        @Test
        @DisplayName("should return paginated revisions")
        void listRevisions_success() {
            Pageable pageable = PageRequest.of(0, 10);
            when(partRepository.existsById(1L)).thenReturn(true);
            when(partRevisionRepository.findByPartIdOrderByRevisionDesc(1L))
                    .thenReturn(List.of(partRevision));
            when(partRevisionMapper.toDto(partRevision)).thenReturn(partRevisionDTO);

            Page<PartRevisionDTO> result = partService.listPartRevisions(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should throw when part not found")
        void listRevisions_partNotFound() {
            Pageable pageable = PageRequest.of(0, 10);
            when(partRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> partService.listPartRevisions(99L, pageable))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should return empty page when offset exceeds size")
        void listRevisions_offsetExceedsSize() {
            Pageable pageable = PageRequest.of(5, 10);
            when(partRepository.existsById(1L)).thenReturn(true);
            when(partRevisionRepository.findByPartIdOrderByRevisionDesc(1L))
                    .thenReturn(List.of(partRevision));

            Page<PartRevisionDTO> result = partService.listPartRevisions(1L, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}
