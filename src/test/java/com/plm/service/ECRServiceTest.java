package com.plm.service;

import com.plm.dto.ECRDTO;
import com.plm.dto.PartDTO;
import com.plm.entity.ECR;
import com.plm.entity.ECRPart;
import com.plm.entity.Part;
import com.plm.entity.User;
import com.plm.mapper.ECRMapper;
import com.plm.mapper.PartMapper;
import com.plm.repository.ECRPartRepository;
import com.plm.repository.ECRRepository;
import com.plm.repository.PartRepository;
import com.plm.repository.UserRepository;
import com.plm.workflow.WorkflowService;
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
class ECRServiceTest {

    @Mock
    private ECRRepository ecrRepository;
    @Mock
    private ECRPartRepository ecrPartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PartRepository partRepository;
    @Mock
    private ECRMapper ecrMapper;
    @Mock
    private PartMapper partMapper;
    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private ECRServiceImpl ecrService;

    private ECR ecr;
    private ECRDTO ecrDTO;
    private User user;
    private Part part;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        ecr = ECR.builder()
                .id(1L)
                .ecrNumber("ECR-001")
                .title("Test ECR")
                .description("A test ECR")
                .status("DRAFT")
                .priority("MEDIUM")
                .build();

        ecrDTO = ECRDTO.builder()
                .id(1L)
                .ecrNumber("ECR-001")
                .title("Test ECR")
                .description("A test ECR")
                .status("DRAFT")
                .priority("MEDIUM")
                .build();

        part = Part.builder()
                .id(1L)
                .partNumber("P-001")
                .name("Test Part")
                .build();
    }

    // ==================== createECR ====================

    @Nested
    @DisplayName("createECR")
    class CreateECR {

        @Test
        @DisplayName("should create ECR successfully")
        void createECR_success() {
            when(ecrRepository.existsByEcrNumber("ECR-001")).thenReturn(false);
            when(ecrMapper.toEntity(any(ECRDTO.class))).thenReturn(ecr);
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ECRDTO result = ecrService.createECR(ecrDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEcrNumber()).isEqualTo("ECR-001");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            verify(ecrRepository).save(ecr);
        }

        @Test
        @DisplayName("should throw when ECR number already exists")
        void createECR_duplicateNumber() {
            when(ecrRepository.existsByEcrNumber("ECR-001")).thenReturn(true);

            assertThatThrownBy(() -> ecrService.createECR(ecrDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should set assigned user when provided")
        void createECR_withAssignedUser() {
            ecrDTO.setAssignedToId(1L);
            when(ecrRepository.existsByEcrNumber("ECR-001")).thenReturn(false);
            when(ecrMapper.toEntity(any(ECRDTO.class))).thenReturn(ecr);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ecrService.createECR(ecrDTO);

            assertThat(ecr.getAssignedTo()).isEqualTo(user);
        }

        @Test
        @DisplayName("should throw when assigned user not found")
        void createECR_assignedUserNotFound() {
            ecrDTO.setAssignedToId(99L);
            when(ecrRepository.existsByEcrNumber("ECR-001")).thenReturn(false);
            when(ecrMapper.toEntity(any(ECRDTO.class))).thenReturn(ecr);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.createECR(ecrDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ==================== getECRById ====================

    @Nested
    @DisplayName("getECRById")
    class GetECRById {

        @Test
        @DisplayName("should return ECR when found")
        void getECRById_found() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecrMapper.toDTO(ecr)).thenReturn(ecrDTO);

            ECRDTO result = ecrService.getECRById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void getECRById_notFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.getECRById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ECR not found");
        }
    }

    // ==================== getECRByNumber ====================

    @Nested
    @DisplayName("getECRByNumber")
    class GetECRByNumber {

        @Test
        @DisplayName("should return ECR when found by number")
        void getECRByNumber_found() {
            when(ecrRepository.findByEcrNumber("ECR-001")).thenReturn(Optional.of(ecr));
            when(ecrMapper.toDTO(ecr)).thenReturn(ecrDTO);

            ECRDTO result = ecrService.getECRByNumber("ECR-001");

            assertThat(result).isNotNull();
            assertThat(result.getEcrNumber()).isEqualTo("ECR-001");
        }

        @Test
        @DisplayName("should throw when ECR number not found")
        void getECRByNumber_notFound() {
            when(ecrRepository.findByEcrNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.getECRByNumber("UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== updateECR ====================

    @Nested
    @DisplayName("updateECR")
    class UpdateECR {

        @Test
        @DisplayName("should update ECR in DRAFT status")
        void updateECR_success() {
            ECRDTO updateDTO = ECRDTO.builder()
                    .title("Updated Title")
                    .description("Updated description")
                    .priority("HIGH")
                    .build();

            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(updateDTO);

            ECRDTO result = ecrService.updateECR(1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void updateECR_notFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.updateECR(99L, ecrDTO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when ECR is not in DRAFT status")
        void updateECR_notDraft() {
            ecr.setStatus("SUBMITTED");
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));

            assertThatThrownBy(() -> ecrService.updateECR(1L, ecrDTO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot update ECR in status");
        }

        @Test
        @DisplayName("should update assigned user when provided")
        void updateECR_withAssignedUser() {
            ecrDTO.setAssignedToId(1L);
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ecrService.updateECR(1L, ecrDTO);

            assertThat(ecr.getAssignedTo()).isEqualTo(user);
        }
    }

    // ==================== deleteECR ====================

    @Nested
    @DisplayName("deleteECR")
    class DeleteECR {

        @Test
        @DisplayName("should delete ECR in DRAFT status")
        void deleteECR_success() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            doNothing().when(ecrRepository).delete(ecr);

            ecrService.deleteECR(1L);

            verify(ecrRepository).delete(ecr);
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void deleteECR_notFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.deleteECR(99L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when ECR is not in DRAFT status")
        void deleteECR_notDraft() {
            ecr.setStatus("SUBMITTED");
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));

            assertThatThrownBy(() -> ecrService.deleteECR(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete ECR in status");
        }
    }

    // ==================== listECRs ====================

    @Nested
    @DisplayName("listECRs")
    class ListECRs {

        @Test
        @DisplayName("should return paginated ECRs")
        void listECRs_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ECR> page = new PageImpl<>(List.of(ecr), pageable, 1);
            when(ecrRepository.findAll(pageable)).thenReturn(page);
            when(ecrMapper.toDTO(ecr)).thenReturn(ecrDTO);

            Page<ECRDTO> result = ecrService.listECRs(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== submitECR ====================

    @Nested
    @DisplayName("submitECR")
    class SubmitECR {

        @Test
        @DisplayName("should submit ECR from DRAFT status")
        void submitECR_success() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(workflowService.startProcess(anyString(), anyString(), anyMap())).thenReturn("process-123");
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ECRDTO result = ecrService.submitECR(1L);

            assertThat(result).isNotNull();
            assertThat(ecr.getStatus()).isEqualTo("SUBMITTED");
            verify(workflowService).startProcess(eq("ecr-process"), eq("ECR-1"), anyMap());
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void submitECR_notFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.submitECR(99L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when ECR is not in DRAFT status")
        void submitECR_notDraft() {
            ecr.setStatus("SUBMITTED");
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));

            assertThatThrownBy(() -> ecrService.submitECR(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot submit ECR in status");
        }
    }

    // ==================== evaluateECR ====================

    @Nested
    @DisplayName("evaluateECR")
    class EvaluateECR {

        @Test
        @DisplayName("should evaluate ECR from SUBMITTED status")
        void evaluateECR_success() {
            ecr.setStatus("SUBMITTED");
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(workflowService.findProcessInstanceByBusinessKey("ECR-1")).thenReturn("process-123");
            when(workflowService.getTasksByProcessInstance("process-123")).thenReturn(Collections.emptyList());
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ECRDTO result = ecrService.evaluateECR(1L);

            assertThat(result).isNotNull();
            assertThat(ecr.getStatus()).isEqualTo("EVALUATED");
        }

        @Test
        @DisplayName("should throw when ECR is not in SUBMITTED status")
        void evaluateECR_notSubmitted() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));

            assertThatThrownBy(() -> ecrService.evaluateECR(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot evaluate ECR in status");
        }
    }

    // ==================== approveECR ====================

    @Nested
    @DisplayName("approveECR")
    class ApproveECR {

        @Test
        @DisplayName("should approve ECR from EVALUATED status")
        void approveECR_success() {
            ecr.setStatus("EVALUATED");
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(workflowService.findProcessInstanceByBusinessKey("ECR-1")).thenReturn("process-123");
            when(workflowService.getTasksByProcessInstance("process-123")).thenReturn(Collections.emptyList());
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ECRDTO result = ecrService.approveECR(1L);

            assertThat(result).isNotNull();
            assertThat(ecr.getStatus()).isEqualTo("APPROVED");
        }

        @Test
        @DisplayName("should throw when ECR is not in EVALUATED status")
        void approveECR_notEvaluated() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));

            assertThatThrownBy(() -> ecrService.approveECR(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot approve ECR in status");
        }
    }

    // ==================== rejectECR ====================

    @Nested
    @DisplayName("rejectECR")
    class RejectECR {

        @Test
        @DisplayName("should reject ECR from EVALUATED status")
        void rejectECR_success() {
            ecr.setStatus("EVALUATED");
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(workflowService.findProcessInstanceByBusinessKey("ECR-1")).thenReturn("process-123");
            when(workflowService.getTasksByProcessInstance("process-123")).thenReturn(Collections.emptyList());
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ECRDTO result = ecrService.rejectECR(1L);

            assertThat(result).isNotNull();
            assertThat(ecr.getStatus()).isEqualTo("REJECTED");
        }

        @Test
        @DisplayName("should throw when ECR is not in EVALUATED status")
        void rejectECR_notEvaluated() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));

            assertThatThrownBy(() -> ecrService.rejectECR(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot reject ECR in status");
        }
    }

    // ==================== assignECR ====================

    @Nested
    @DisplayName("assignECR")
    class AssignECR {

        @Test
        @DisplayName("should assign ECR to user")
        void assignECR_success() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ecrRepository.save(any(ECR.class))).thenReturn(ecr);
            when(ecrMapper.toDTO(any(ECR.class))).thenReturn(ecrDTO);

            ECRDTO result = ecrService.assignECR(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(ecr.getAssignedTo()).isEqualTo(user);
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void assignECR_ecrNotFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.assignECR(99L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when user not found")
        void assignECR_userNotFound() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.assignECR(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ==================== getAffectedParts ====================

    @Nested
    @DisplayName("getAffectedParts")
    class GetAffectedParts {

        @Test
        @DisplayName("should return affected parts")
        void getAffectedParts_success() {
            ECRPart ecrPart = ECRPart.builder().ecr(ecr).part(part).build();
            PartDTO partDTO = PartDTO.builder().id(1L).partNumber("P-001").build();

            when(ecrRepository.existsById(1L)).thenReturn(true);
            when(ecrPartRepository.findByEcrId(1L)).thenReturn(List.of(ecrPart));
            when(partMapper.toDto(part)).thenReturn(partDTO);

            List<PartDTO> result = ecrService.getAffectedParts(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPartNumber()).isEqualTo("P-001");
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void getAffectedParts_ecrNotFound() {
            when(ecrRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> ecrService.getAffectedParts(99L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== addAffectedPart ====================

    @Nested
    @DisplayName("addAffectedPart")
    class AddAffectedPart {

        @Test
        @DisplayName("should add affected part")
        void addAffectedPart_success() {
            PartDTO partDTO = PartDTO.builder().id(1L).partNumber("P-001").build();

            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(ecrPartRepository.existsByEcrIdAndPartId(1L, 1L)).thenReturn(false);
            when(ecrPartRepository.save(any(ECRPart.class))).thenReturn(null);
            when(partMapper.toDto(part)).thenReturn(partDTO);

            PartDTO result = ecrService.addAffectedPart(1L, 1L);

            assertThat(result).isNotNull();
            verify(ecrPartRepository).save(any(ECRPart.class));
        }

        @Test
        @DisplayName("should throw when part already affected")
        void addAffectedPart_alreadyAffected() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(partRepository.findById(1L)).thenReturn(Optional.of(part));
            when(ecrPartRepository.existsByEcrIdAndPartId(1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> ecrService.addAffectedPart(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already affected");
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void addAffectedPart_ecrNotFound() {
            when(ecrRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.addAffectedPart(99L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when part not found")
        void addAffectedPart_partNotFound() {
            when(ecrRepository.findById(1L)).thenReturn(Optional.of(ecr));
            when(partRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ecrService.addAffectedPart(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part not found");
        }
    }

    // ==================== removeAffectedPart ====================

    @Nested
    @DisplayName("removeAffectedPart")
    class RemoveAffectedPart {

        @Test
        @DisplayName("should remove affected part")
        void removeAffectedPart_success() {
            when(ecrRepository.existsById(1L)).thenReturn(true);
            when(ecrPartRepository.existsByEcrIdAndPartId(1L, 1L)).thenReturn(true);
            doNothing().when(ecrPartRepository).deleteByEcrIdAndPartId(1L, 1L);

            ecrService.removeAffectedPart(1L, 1L);

            verify(ecrPartRepository).deleteByEcrIdAndPartId(1L, 1L);
        }

        @Test
        @DisplayName("should throw when ECR not found")
        void removeAffectedPart_ecrNotFound() {
            when(ecrRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> ecrService.removeAffectedPart(99L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when part not affected")
        void removeAffectedPart_notAffected() {
            when(ecrRepository.existsById(1L)).thenReturn(true);
            when(ecrPartRepository.existsByEcrIdAndPartId(1L, 1L)).thenReturn(false);

            assertThatThrownBy(() -> ecrService.removeAffectedPart(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not affected");
        }
    }
}
