package com.plm.service;

import com.plm.dto.ComponentDraftDTO;
import com.plm.dto.ECOApprovalDTO;
import com.plm.dto.ECODTO;
import com.plm.entity.*;
import com.plm.mapper.ECOApprovalMapper;
import com.plm.mapper.ECOMapper;
import com.plm.repository.*;
import com.plm.workflow.WorkflowService;
import jakarta.persistence.EntityNotFoundException;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ECOServiceTest {

    @Mock
    private ECORepository ecoRepository;
    @Mock
    private ECOApprovalRepository ecoApprovalRepository;
    @Mock
    private ECRRepository ecrRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BOMRepository bomRepository;
    @Mock
    private BOMItemRepository bomItemRepository;
    @Mock
    private PartRevisionRepository partRevisionRepository;
    @Mock
    private ECOMapper ecoMapper;
    @Mock
    private ECOApprovalMapper ecoApprovalMapper;
    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private ECOServiceImpl ecoService;

    private ECO eco;
    private ECODTO ecoDTO;
    private ECR ecr;
    private User user;
    private BOM bom;
    private PartRevision partRevision;
    private Part part;

    @BeforeEach
    void setUp() {
        ecr = ECR.builder()
                .id(1L)
                .ecrNumber("ECR-001")
                .title("Test ECR")
                .build();

        eco = ECO.builder()
                .id(1L)
                .ecoNumber("ECO-001")
                .title("Test ECO")
                .description("A test ECO")
                .status("DRAFT")
                .type("ECO")
                .build();

        ecoDTO = ECODTO.builder()
                .id(1L)
                .ecoNumber("ECO-001")
                .title("Test ECO")
                .description("A test ECO")
                .status("DRAFT")
                .type("ECO")
                .build();

        user = User.builder()
                .id(1L)
                .username("approver")
                .email("approver@example.com")
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
                .build();

        bom = BOM.builder()
                .id(1L)
                .name("Test BOM")
                .assembly(Assembly.builder().id(1L).build())
                .status("DRAFT")
                .build();
    }

    // ==================== createECO ====================

    @Nested
    @DisplayName("createECO")
    class CreateECO {

        @Test
        @DisplayName("should create ECO successfully")
        void createECO_success() {
            when(ecoRepository.existsByEcoNumber("ECO-001")).thenReturn(false);
            when(ecoMapper.toEntity(any(ECODTO.class))).thenReturn(eco);
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoMapper.toDTO(any(ECO.class))).thenReturn(ecoDTO);

            ECODTO result = ecoService.createECO(ecoDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEcoNumber()).isEqualTo("ECO-001");
            assertThat(eco.getStatus()).isEqualTo("DRAFT");
            verify(ecoRepository).save(eco);
        }

        @Test
        @DisplayName("should throw when ECO number already exists")
        void createECO_duplicateNumber() {
            when(ecoRepository.existsByEcoNumber("ECO-001")).thenReturn(true);

            assertThatThrownBy(() -> ecoService.createECO(ecoDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should link to ECR when provided")
        void createECO_withECR() {
            ecoDTO.setEcrId(1L);
            when(ecoRepository.existsByEcoNumber("ECO-001")).thenReturn(false);
            when(ecoMapper.toEntity(any(ECODTO.class))).thenReturn(eco);
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoMapper.toDTO(any(ECO.class))).thenReturn(ecoDTO);

            ecoService.createECO(ecoDTO);

            assertThat(eco.getEcr()).isEqualTo(ecr);
        }

        @Test
        @DisplayName("should throw when linked ECR not found")
        void createECO_ecrNotFound() {
            ecoDTO.setEcrId(99L);
            when(ecoRepository.existsByEcoNumber("ECO-001")).thenReturn(false);
            when(ecoMapper.toEntity(any(ECODTO.class))).thenReturn(eco);
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.createECO(ecoDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("ECR not found");
        }
    }

    // ==================== getECOById ====================

    @Nested
    @DisplayName("getECOById")
    class GetECOById {

        @Test
        @DisplayName("should return ECO when found")
        void getECOById_found() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(ecoMapper.toDTO(eco)).thenReturn(ecoDTO);

            ECODTO result = ecoService.getECOById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when ECO not found")
        void getECOById_notFound() {
            when(ecoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.getECOById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("ECO not found");
        }
    }

    // ==================== getECOByNumber ====================

    @Nested
    @DisplayName("getECOByNumber")
    class GetECOByNumber {

        @Test
        @DisplayName("should return ECO when found by number")
        void getECOByNumber_found() {
            when(ecoRepository.findByEcoNumber("ECO-001")).thenReturn(Optional.of(eco));
            when(ecoMapper.toDTO(eco)).thenReturn(ecoDTO);

            ECODTO result = ecoService.getECOByNumber("ECO-001");

            assertThat(result).isNotNull();
            assertThat(result.getEcoNumber()).isEqualTo("ECO-001");
        }

        @Test
        @DisplayName("should throw when ECO number not found")
        void getECOByNumber_notFound() {
            when(ecoRepository.findByEcoNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.getECOByNumber("UNKNOWN"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== updateECO ====================

    @Nested
    @DisplayName("updateECO")
    class UpdateECO {

        @Test
        @DisplayName("should update ECO in DRAFT status")
        void updateECO_success() {
            ECODTO updateDTO = ECODTO.builder()
                    .ecoNumber("ECO-001")
                    .title("Updated Title")
                    .description("Updated description")
                    .type("ECR")
                    .build();

            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoMapper.toDTO(any(ECO.class))).thenReturn(updateDTO);

            ECODTO result = ecoService.updateECO(1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("should throw when ECO not found")
        void updateECO_notFound() {
            when(ecoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.updateECO(99L, ecoDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when ECO is not in DRAFT status")
        void updateECO_notDraft() {
            eco.setStatus("IN_PROGRESS");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.updateECO(1L, ecoDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot update ECO");
        }

        @Test
        @DisplayName("should throw when changing to duplicate ECO number")
        void updateECO_duplicateNumber() {
            ECODTO updateDTO = ECODTO.builder()
                    .ecoNumber("ECO-002")
                    .title("Updated")
                    .build();

            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(ecoRepository.existsByEcoNumber("ECO-002")).thenReturn(true);

            assertThatThrownBy(() -> ecoService.updateECO(1L, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }
    }

    // ==================== deleteECO ====================

    @Nested
    @DisplayName("deleteECO")
    class DeleteECO {

        @Test
        @DisplayName("should delete ECO in DRAFT status")
        void deleteECO_success() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            doNothing().when(ecoRepository).delete(eco);

            ecoService.deleteECO(1L);

            verify(ecoRepository).delete(eco);
        }

        @Test
        @DisplayName("should throw when ECO not found")
        void deleteECO_notFound() {
            when(ecoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.deleteECO(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when ECO is not in DRAFT status")
        void deleteECO_notDraft() {
            eco.setStatus("IN_PROGRESS");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.deleteECO(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete ECO");
        }
    }

    // ==================== listECOs ====================

    @Nested
    @DisplayName("listECOs")
    class ListECOs {

        @Test
        @DisplayName("should return paginated ECOs")
        void listECOs_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ECO> page = new PageImpl<>(List.of(eco), pageable, 1);
            when(ecoRepository.findAll(pageable)).thenReturn(page);
            when(ecoMapper.toDTO(eco)).thenReturn(ecoDTO);

            Page<ECODTO> result = ecoService.listECOs(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== submitECO ====================

    @Nested
    @DisplayName("submitECO")
    class SubmitECO {

        @Test
        @DisplayName("should submit ECO from DRAFT status")
        void submitECO_success() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(workflowService.startProcess(eq("eco-approval"), anyMap())).thenReturn("process-123");
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoMapper.toDTO(any(ECO.class))).thenReturn(ecoDTO);

            ECODTO result = ecoService.submitECO(1L);

            assertThat(result).isNotNull();
            assertThat(eco.getStatus()).isEqualTo("IN_PROGRESS");
            assertThat(eco.getCurrentStage()).isEqualTo("ENGINEERING_REVIEW");
            assertThat(eco.getProcessInstanceId()).isEqualTo("process-123");
        }

        @Test
        @DisplayName("should throw when ECO not found")
        void submitECO_notFound() {
            when(ecoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.submitECO(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when ECO is not in DRAFT status")
        void submitECO_notDraft() {
            eco.setStatus("IN_PROGRESS");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.submitECO(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot submit ECO");
        }
    }

    // ==================== approveECO ====================

    @Nested
    @DisplayName("approveECO")
    class ApproveECO {

        @Test
        @DisplayName("should approve ECO at first stage")
        void approveECO_firstStage() {
            eco.setStatus("IN_PROGRESS");
            eco.setCurrentStage("ENGINEERING_REVIEW");
            eco.setProcessInstanceId("process-123");

            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ecoApprovalRepository.save(any(ECOApproval.class))).thenReturn(null);
            when(workflowService.getTasksByProcessInstance("process-123")).thenReturn(Collections.emptyList());
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoApprovalMapper.toDTO(any())).thenReturn(
                    ECOApprovalDTO.builder().id(1L).stage("ENGINEERING_REVIEW").decision("APPROVED").build());

            ECOApprovalDTO result = ecoService.approveECO(1L, 1L, "ENGINEERING_REVIEW", "Looks good");

            assertThat(result).isNotNull();
            assertThat(result.getDecision()).isEqualTo("APPROVED");
            // Should advance to next stage
            assertThat(eco.getCurrentStage()).isEqualTo("MANAGER_APPROVAL");
        }

        @Test
        @DisplayName("should mark as fully approved at last stage")
        void approveECO_lastStage() {
            eco.setStatus("IN_PROGRESS");
            eco.setCurrentStage("MANAGER_APPROVAL");
            eco.setProcessInstanceId("process-123");

            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ecoApprovalRepository.save(any(ECOApproval.class))).thenReturn(null);
            when(workflowService.getTasksByProcessInstance("process-123")).thenReturn(Collections.emptyList());
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoApprovalMapper.toDTO(any())).thenReturn(
                    ECOApprovalDTO.builder().id(1L).stage("MANAGER_APPROVAL").decision("APPROVED").build());

            ecoService.approveECO(1L, 1L, "MANAGER_APPROVAL", "Approved");

            assertThat(eco.getStatus()).isEqualTo("APPROVED");
            assertThat(eco.getCurrentStage()).isNull();
        }

        @Test
        @DisplayName("should throw when ECO is not in IN_PROGRESS status")
        void approveECO_notInProgress() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.approveECO(1L, 1L, "ENGINEERING_REVIEW", "comment"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot approve ECO");
        }

        @Test
        @DisplayName("should throw when stage does not match")
        void approveECO_wrongStage() {
            eco.setStatus("IN_PROGRESS");
            eco.setCurrentStage("MANAGER_APPROVAL");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.approveECO(1L, 1L, "ENGINEERING_REVIEW", "comment"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Expected stage");
        }

        @Test
        @DisplayName("should throw when approver not found")
        void approveECO_approverNotFound() {
            eco.setStatus("IN_PROGRESS");
            eco.setCurrentStage("ENGINEERING_REVIEW");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.approveECO(1L, 99L, "ENGINEERING_REVIEW", "comment"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ==================== rejectECO ====================

    @Nested
    @DisplayName("rejectECO")
    class RejectECO {

        @Test
        @DisplayName("should reject ECO and move back to DRAFT")
        void rejectECO_success() {
            eco.setStatus("IN_PROGRESS");
            eco.setCurrentStage("ENGINEERING_REVIEW");
            eco.setProcessInstanceId("process-123");

            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ecoApprovalRepository.save(any(ECOApproval.class))).thenReturn(null);
            when(workflowService.getTasksByProcessInstance("process-123")).thenReturn(Collections.emptyList());
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoApprovalMapper.toDTO(any())).thenReturn(
                    ECOApprovalDTO.builder().id(1L).stage("ENGINEERING_REVIEW").decision("REJECTED").build());

            ECOApprovalDTO result = ecoService.rejectECO(1L, 1L, "ENGINEERING_REVIEW", "Needs work");

            assertThat(result).isNotNull();
            assertThat(result.getDecision()).isEqualTo("REJECTED");
            assertThat(eco.getStatus()).isEqualTo("DRAFT");
            assertThat(eco.getCurrentStage()).isNull();
        }

        @Test
        @DisplayName("should throw when ECO is not in IN_PROGRESS status")
        void rejectECO_notInProgress() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.rejectECO(1L, 1L, "ENGINEERING_REVIEW", "comment"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot reject ECO");
        }
    }

    // ==================== applyECO ====================

    @Nested
    @DisplayName("applyECO")
    class ApplyECO {

        @Test
        @DisplayName("should apply ECO from APPROVED status")
        void applyECO_success() {
            eco.setStatus("APPROVED");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoMapper.toDTO(any(ECO.class))).thenReturn(ecoDTO);

            ECODTO result = ecoService.applyECO(1L);

            assertThat(result).isNotNull();
            assertThat(eco.getStatus()).isEqualTo("APPLIED");
            assertThat(eco.getAppliedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when ECO is not in APPROVED status")
        void applyECO_notApproved() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.applyECO(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot apply ECO");
        }
    }

    // ==================== closeECO ====================

    @Nested
    @DisplayName("closeECO")
    class CloseECO {

        @Test
        @DisplayName("should close ECO from APPLIED status")
        void closeECO_success() {
            eco.setStatus("APPLIED");
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(ecoRepository.save(any(ECO.class))).thenReturn(eco);
            when(ecoMapper.toDTO(any(ECO.class))).thenReturn(ecoDTO);

            ECODTO result = ecoService.closeECO(1L);

            assertThat(result).isNotNull();
            assertThat(eco.getStatus()).isEqualTo("CLOSED");
        }

        @Test
        @DisplayName("should throw when ECO is not in APPLIED status")
        void closeECO_notApplied() {
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.closeECO(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot close ECO");
        }
    }

    // ==================== addComponentDraft ====================

    @Nested
    @DisplayName("addComponentDraft")
    class AddComponentDraft {

        @Test
        @DisplayName("should add component draft in DRAFT status")
        void addComponentDraft_success() {
            ComponentDraftDTO draftDTO = ComponentDraftDTO.builder()
                    .bomId(1L)
                    .partRevisionId(1L)
                    .quantity(new BigDecimal("5"))
                    .designator("R1")
                    .action("ADD")
                    .build();

            BOMItem savedItem = BOMItem.builder()
                    .id(10L)
                    .bom(bom)
                    .partRevision(partRevision)
                    .quantity(new BigDecimal("5"))
                    .designator("R1")
                    .build();

            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(partRevisionRepository.findById(1L)).thenReturn(Optional.of(partRevision));
            when(bomItemRepository.save(any(BOMItem.class))).thenReturn(savedItem);

            ComponentDraftDTO result = ecoService.addComponentDraft(1L, draftDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEcoId()).isEqualTo(1L);
            assertThat(result.getAction()).isEqualTo("ADD");
        }

        @Test
        @DisplayName("should throw when ECO is not in DRAFT or IN_PROGRESS status")
        void addComponentDraft_invalidStatus() {
            eco.setStatus("APPROVED");
            ComponentDraftDTO draftDTO = ComponentDraftDTO.builder().bomId(1L).partRevisionId(1L).build();
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));

            assertThatThrownBy(() -> ecoService.addComponentDraft(1L, draftDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot add component draft");
        }

        @Test
        @DisplayName("should throw when BOM not found")
        void addComponentDraft_bomNotFound() {
            ComponentDraftDTO draftDTO = ComponentDraftDTO.builder().bomId(99L).partRevisionId(1L).build();
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(bomRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.addComponentDraft(1L, draftDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("BOM not found");
        }

        @Test
        @DisplayName("should throw when part revision not found")
        void addComponentDraft_partRevisionNotFound() {
            ComponentDraftDTO draftDTO = ComponentDraftDTO.builder().bomId(1L).partRevisionId(99L).build();
            when(ecoRepository.findById(1L)).thenReturn(Optional.of(eco));
            when(bomRepository.findById(1L)).thenReturn(Optional.of(bom));
            when(partRevisionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.addComponentDraft(1L, draftDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Part revision not found");
        }
    }

    // ==================== listComponentDrafts ====================

    @Nested
    @DisplayName("listComponentDrafts")
    class ListComponentDrafts {

        @Test
        @DisplayName("should return empty list (not yet implemented)")
        void listComponentDrafts_empty() {
            when(ecoRepository.existsById(1L)).thenReturn(true);

            List<ComponentDraftDTO> result = ecoService.listComponentDrafts(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when ECO not found")
        void listComponentDrafts_notFound() {
            when(ecoRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> ecoService.listComponentDrafts(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== convertECRToECO ====================

    @Nested
    @DisplayName("convertECRToECO")
    class ConvertECRToECO {

        @Test
        @DisplayName("should convert ECR to ECO")
        void convertECRToECO_success() {
            ECO newEco = ECO.builder()
                    .id(2L)
                    .ecoNumber("ECO-ECR-001")
                    .title("Test ECR")
                    .description("A test ECR")
                    .status("DRAFT")
                    .type("ECO")
                    .ecr(ecr)
                    .build();

            ECODTO newEcoDTO = ECODTO.builder()
                    .id(2L)
                    .ecoNumber("ECO-ECR-001")
                    .title("Test ECR")
                    .status("DRAFT")
                    .build();

            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecoRepository.findByEcrId(1L)).thenReturn(Collections.emptyList());
            when(ecoRepository.existsByEcoNumber("ECO-ECR-001")).thenReturn(false);
            when(ecoRepository.save(any(ECO.class))).thenReturn(newEco);
            when(ecoMapper.toDTO(newEco)).thenReturn(newEcoDTO);

            ECODTO result = ecoService.convertECRToECO(1L);

            assertThat(result).isNotNull();
            assertThat(result.getEcoNumber()).isEqualTo("ECO-ECR-001");
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void convertECRToECO_ecrNotFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecoService.convertECRToECO(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when ECO already exists for ECR")
        void convertECRToECO_alreadyExists() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecoRepository.findByEcrId(1L)).thenReturn(List.of(eco));

            assertThatThrownBy(() -> ecoService.convertECRToECO(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ECO already exists");
        }

        @Test
        @DisplayName("should throw when generated ECO number already exists")
        void convertECRToECO_numberConflict() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecoRepository.findByEcrId(1L)).thenReturn(Collections.emptyList());
            when(ecoRepository.existsByEcoNumber("ECO-ECR-001")).thenReturn(true);

            assertThatThrownBy(() -> ecoService.convertECRToECO(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }
    }
}
