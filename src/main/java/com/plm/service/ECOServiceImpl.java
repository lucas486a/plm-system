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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ECOServiceImpl implements ECOService {

    private final ECORepository ecoRepository;
    private final ECOApprovalRepository ecoApprovalRepository;
    private final ECRRepository ecrRepository;
    private final UserRepository userRepository;
    private final BOMRepository bomRepository;
    private final BOMItemRepository bomItemRepository;
    private final PartRevisionRepository partRevisionRepository;
    private final ECOMapper ecoMapper;
    private final ECOApprovalMapper ecoApprovalMapper;
    private final WorkflowService workflowService;

    /**
     * Multi-stage approval pipeline (sequential only, no parallel).
     * ECO flows: DRAFT -> IN_PROGRESS (stages) -> APPROVED -> APPLIED -> CLOSED
     */
    private static final List<String> APPROVAL_STAGES = List.of(
            "ENGINEERING_REVIEW",
            "MANAGER_APPROVAL"
    );

    // =========================================================================
    // CRUD Operations
    // =========================================================================

    @Override
    @Transactional
    public ECODTO createECO(ECODTO ecoDTO) {
        log.info("Creating ECO with number: {}", ecoDTO.getEcoNumber());

        if (ecoRepository.existsByEcoNumber(ecoDTO.getEcoNumber())) {
            throw new IllegalArgumentException("ECO with number " + ecoDTO.getEcoNumber() + " already exists");
        }

        ECO eco = ecoMapper.toEntity(ecoDTO);
        eco.setStatus("DRAFT");
        eco.setCurrentStage(null);

        // Link to ECR if provided
        if (ecoDTO.getEcrId() != null) {
            ECR ecr = ecrRepository.findById(ecoDTO.getEcrId())
                    .orElseThrow(() -> new EntityNotFoundException("ECR not found with id: " + ecoDTO.getEcrId()));
            eco.setEcr(ecr);
        }

        ECO savedEco = ecoRepository.save(eco);
        log.info("Created ECO with id: {}", savedEco.getId());
        return ecoMapper.toDTO(savedEco);
    }

    @Override
    public ECODTO getECOById(Long id) {
        log.debug("Fetching ECO by id: {}", id);
        ECO eco = ecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + id));
        return ecoMapper.toDTO(eco);
    }

    @Override
    public ECODTO getECOByNumber(String ecoNumber) {
        log.debug("Fetching ECO by number: {}", ecoNumber);
        ECO eco = ecoRepository.findByEcoNumber(ecoNumber)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with number: " + ecoNumber));
        return ecoMapper.toDTO(eco);
    }

    @Override
    @Transactional
    public ECODTO updateECO(Long id, ECODTO ecoDTO) {
        log.info("Updating ECO with id: {}", id);

        ECO existingEco = ecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + id));

        // Only allow updates in DRAFT status
        if (!"DRAFT".equals(existingEco.getStatus())) {
            throw new IllegalStateException("Cannot update ECO in " + existingEco.getStatus() + " status");
        }

        // Check if ecoNumber is being changed and if new number already exists
        if (!existingEco.getEcoNumber().equals(ecoDTO.getEcoNumber()) &&
                ecoRepository.existsByEcoNumber(ecoDTO.getEcoNumber())) {
            throw new IllegalArgumentException("ECO with number " + ecoDTO.getEcoNumber() + " already exists");
        }

        existingEco.setEcoNumber(ecoDTO.getEcoNumber());
        existingEco.setTitle(ecoDTO.getTitle());
        existingEco.setDescription(ecoDTO.getDescription());
        existingEco.setType(ecoDTO.getType());
        existingEco.setEffectiveDate(ecoDTO.getEffectiveDate());

        // Update ECR link if provided
        if (ecoDTO.getEcrId() != null) {
            ECR ecr = ecrRepository.findById(ecoDTO.getEcrId())
                    .orElseThrow(() -> new EntityNotFoundException("ECR not found with id: " + ecoDTO.getEcrId()));
            existingEco.setEcr(ecr);
        } else {
            existingEco.setEcr(null);
        }

        ECO updatedEco = ecoRepository.save(existingEco);
        log.info("Updated ECO with id: {}", updatedEco.getId());
        return ecoMapper.toDTO(updatedEco);
    }

    @Override
    @Transactional
    public void deleteECO(Long id) {
        log.info("Deleting ECO with id: {}", id);

        ECO eco = ecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + id));

        // Only allow deletion in DRAFT status
        if (!"DRAFT".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot delete ECO in " + eco.getStatus() + " status");
        }

        ecoRepository.delete(eco);
        log.info("Deleted ECO with id: {}", id);
    }

    @Override
    public Page<ECODTO> listECOs(Pageable pageable) {
        log.debug("Listing ECOs with pagination: {}", pageable);
        return ecoRepository.findAll(pageable)
                .map(ecoMapper::toDTO);
    }

    // =========================================================================
    // Workflow Operations - State Machine
    // DRAFT -> IN_PROGRESS -> APPROVED -> APPLIED -> CLOSED
    // Rejection: IN_PROGRESS -> DRAFT
    // =========================================================================

    @Override
    @Transactional
    public ECODTO submitECO(Long id) {
        log.info("Submitting ECO with id: {}", id);

        ECO eco = ecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + id));

        // Only allow submission from DRAFT status
        if (!"DRAFT".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot submit ECO in " + eco.getStatus() + " status");
        }

        // Start Flowable workflow process
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("ecoId", eco.getId());
        processVariables.put("ecoNumber", eco.getEcoNumber());

        String processInstanceId = workflowService.startProcess("eco-approval", processVariables);
        log.info("Started ECO approval workflow: processInstance={}, ecoId={}", processInstanceId, id);

        eco.setStatus("IN_PROGRESS");
        eco.setCurrentStage(APPROVAL_STAGES.get(0)); // Start with first stage
        eco.setProcessInstanceId(processInstanceId);

        ECO updatedEco = ecoRepository.save(eco);
        log.info("Submitted ECO id: {}, current stage: {}, processInstance: {}",
                id, updatedEco.getCurrentStage(), processInstanceId);
        return ecoMapper.toDTO(updatedEco);
    }

    @Override
    @Transactional
    public ECOApprovalDTO approveECO(Long ecoId, Long approverId, String stage, String comments) {
        log.info("Approving ECO id: {} at stage: {} by approver: {}", ecoId, stage, approverId);

        ECO eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + ecoId));

        // Must be in IN_PROGRESS status
        if (!"IN_PROGRESS".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot approve ECO in " + eco.getStatus() + " status");
        }

        // Validate stage matches current stage
        if (!stage.equals(eco.getCurrentStage())) {
            throw new IllegalStateException("Expected stage " + eco.getCurrentStage() + " but got " + stage);
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + approverId));

        // Record approval decision
        ECOApproval approval = ECOApproval.builder()
                .eco(eco)
                .stage(stage)
                .approver(approver)
                .decision("APPROVED")
                .comments(comments)
                .timestamp(OffsetDateTime.now())
                .build();

        ECOApproval savedApproval = ecoApprovalRepository.save(approval);

        // Complete the Flowable task for this stage
        completeWorkflowTask(eco, stage, true);

        // Advance to next stage or mark as fully approved
        int currentStageIndex = APPROVAL_STAGES.indexOf(stage);
        if (currentStageIndex < APPROVAL_STAGES.size() - 1) {
            // Move to next approval stage
            eco.setCurrentStage(APPROVAL_STAGES.get(currentStageIndex + 1));
            log.info("ECO id: {} advanced to stage: {}", ecoId, eco.getCurrentStage());
        } else {
            // All stages complete - mark as approved
            // Note: The ECOWorkflowListener will also handle this when the process ends
            eco.setStatus("APPROVED");
            eco.setCurrentStage(null);
            log.info("ECO id: {} fully approved", ecoId);
        }

        ecoRepository.save(eco);
        return ecoApprovalMapper.toDTO(savedApproval);
    }

    @Override
    @Transactional
    public ECOApprovalDTO rejectECO(Long ecoId, Long approverId, String stage, String comments) {
        log.info("Rejecting ECO id: {} at stage: {} by approver: {}", ecoId, stage, approverId);

        ECO eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + ecoId));

        // Must be in IN_PROGRESS status
        if (!"IN_PROGRESS".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot reject ECO in " + eco.getStatus() + " status");
        }

        // Validate stage matches current stage
        if (!stage.equals(eco.getCurrentStage())) {
            throw new IllegalStateException("Expected stage " + eco.getCurrentStage() + " but got " + stage);
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + approverId));

        // Record rejection decision
        ECOApproval approval = ECOApproval.builder()
                .eco(eco)
                .stage(stage)
                .approver(approver)
                .decision("REJECTED")
                .comments(comments)
                .timestamp(OffsetDateTime.now())
                .build();

        ECOApproval savedApproval = ecoApprovalRepository.save(approval);

        // Complete the Flowable task with rejection
        completeWorkflowTask(eco, stage, false);

        // Rejection sends ECO back to DRAFT
        // Note: The ECOWorkflowListener will also handle this when the process ends
        eco.setStatus("DRAFT");
        eco.setCurrentStage(null);
        ecoRepository.save(eco);

        log.info("ECO id: {} rejected at stage: {}, moved back to DRAFT", ecoId, stage);
        return ecoApprovalMapper.toDTO(savedApproval);
    }

    @Override
    @Transactional
    public ECODTO applyECO(Long id) {
        log.info("Applying ECO with id: {}", id);

        ECO eco = ecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + id));

        // Only allow application from APPROVED status
        if (!"APPROVED".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot apply ECO in " + eco.getStatus() + " status");
        }

        // Apply component draft changes to BOM
        // Component drafts are BOM items created under this ECO context.
        // In a full implementation, we would iterate over all draft BOM items
        // and apply ADD/REMOVE/MODIFY actions to the target BOMs.
        // For now, the ECO status transition is the core mechanism.

        eco.setStatus("APPLIED");
        eco.setAppliedAt(OffsetDateTime.now());

        ECO updatedEco = ecoRepository.save(eco);
        log.info("Applied ECO with id: {}", id);
        return ecoMapper.toDTO(updatedEco);
    }

    @Override
    @Transactional
    public ECODTO closeECO(Long id) {
        log.info("Closing ECO with id: {}", id);

        ECO eco = ecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + id));

        // Only allow closure from APPLIED status
        if (!"APPLIED".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot close ECO in " + eco.getStatus() + " status");
        }

        eco.setStatus("CLOSED");

        ECO updatedEco = ecoRepository.save(eco);
        log.info("Closed ECO with id: {}", id);
        return ecoMapper.toDTO(updatedEco);
    }

    // =========================================================================
    // Workflow Integration Helpers
    // =========================================================================

    /**
     * Complete the Flowable user task for the given ECO and stage.
     * Finds the active task in the process instance and completes it with the approval decision.
     *
     * @param eco      the ECO entity (must have processInstanceId set)
     * @param stage    the current approval stage
     * @param approved true if approved, false if rejected
     */
    private void completeWorkflowTask(ECO eco, String stage, boolean approved) {
        String processInstanceId = eco.getProcessInstanceId();
        if (processInstanceId == null) {
            log.warn("ECO id: {} has no processInstanceId, skipping workflow task completion", eco.getId());
            return;
        }

        List<Task> tasks = workflowService.getTasksByProcessInstance(processInstanceId);
        if (tasks.isEmpty()) {
            log.warn("No active tasks found for ECO id: {} process instance: {}", eco.getId(), processInstanceId);
            return;
        }

        // Complete the first active task (sequential flow, only one task at a time)
        Task activeTask = tasks.get(0);
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", approved);
        variables.put("stage", stage);
        variables.put("approverComments", approved ? "Approved" : "Rejected");

        workflowService.completeTask(activeTask.getId(), variables);
        log.info("Completed workflow task {} for ECO id: {} stage: {} approved: {}",
                activeTask.getId(), eco.getId(), stage, approved);
    }

    // =========================================================================
    // Component Drafts - Proposed BOM changes under an ECO
    // =========================================================================

    @Override
    @Transactional
    public ComponentDraftDTO addComponentDraft(Long ecoId, ComponentDraftDTO draftDTO) {
        log.info("Adding component draft to ECO id: {}", ecoId);

        ECO eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new EntityNotFoundException("ECO not found with id: " + ecoId));

        // Only allow adding drafts in DRAFT or IN_PROGRESS status
        if (!"DRAFT".equals(eco.getStatus()) && !"IN_PROGRESS".equals(eco.getStatus())) {
            throw new IllegalStateException("Cannot add component draft to ECO in " + eco.getStatus() + " status");
        }

        // Validate BOM exists
        BOM bom = bomRepository.findById(draftDTO.getBomId())
                .orElseThrow(() -> new EntityNotFoundException("BOM not found with id: " + draftDTO.getBomId()));

        // Validate part revision exists
        PartRevision partRevision = partRevisionRepository.findById(draftDTO.getPartRevisionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Part revision not found with id: " + draftDTO.getPartRevisionId()));

        // Create BOM item as a draft linked to this ECO's context
        BOMItem bomItem = BOMItem.builder()
                .bom(bom)
                .partRevision(partRevision)
                .quantity(draftDTO.getQuantity() != null ? draftDTO.getQuantity() : BigDecimal.ONE)
                .designator(draftDTO.getDesignator())
                .findNumber(draftDTO.getFindNumber())
                .isMounted(draftDTO.getIsMounted() != null ? draftDTO.getIsMounted() : true)
                .comment(draftDTO.getComment())
                .scrapFactor(draftDTO.getScrapFactor() != null ? draftDTO.getScrapFactor() : BigDecimal.ZERO)
                .build();

        BOMItem savedItem = bomItemRepository.save(bomItem);

        ComponentDraftDTO result = ComponentDraftDTO.builder()
                .id(savedItem.getId())
                .ecoId(ecoId)
                .bomId(savedItem.getBom().getId())
                .partRevisionId(savedItem.getPartRevision().getId())
                .partRevisionRevision(savedItem.getPartRevision().getRevision())
                .quantity(savedItem.getQuantity())
                .designator(savedItem.getDesignator())
                .findNumber(savedItem.getFindNumber())
                .isMounted(savedItem.getIsMounted())
                .comment(savedItem.getComment())
                .scrapFactor(savedItem.getScrapFactor())
                .action(draftDTO.getAction())
                .build();

        log.info("Added component draft id: {} to ECO id: {}", savedItem.getId(), ecoId);
        return result;
    }

    @Override
    public List<ComponentDraftDTO> listComponentDrafts(Long ecoId) {
        log.debug("Listing component drafts for ECO id: {}", ecoId);

        if (!ecoRepository.existsById(ecoId)) {
            throw new EntityNotFoundException("ECO not found with id: " + ecoId);
        }

        // Component drafts are BOM items created in the context of this ECO.
        // A full implementation would require an eco_id FK on bom_items or a
        // separate join table. For now, return empty list as the schema does not
        // yet have a direct ECO->BOMItem relationship.
        return Collections.emptyList();
    }

    // =========================================================================
    // ECR to ECO Conversion
    // =========================================================================

    @Override
    @Transactional
    public ECODTO convertECRToECO(Long ecrId) {
        log.info("Converting ECR id: {} to ECO", ecrId);

        ECR ecr = ecrRepository.findById(ecrId)
                .orElseThrow(() -> new EntityNotFoundException("ECR not found with id: " + ecrId));

        // Check if an ECO already exists for this ECR
        List<ECO> existingECOs = ecoRepository.findByEcrId(ecrId);
        if (!existingECOs.isEmpty()) {
            throw new IllegalArgumentException("ECO already exists for ECR id: " + ecrId);
        }

        // Generate ECO number from ECR number
        String ecoNumber = "ECO-" + ecr.getEcrNumber();

        // Ensure generated number is unique
        if (ecoRepository.existsByEcoNumber(ecoNumber)) {
            throw new IllegalArgumentException("ECO with number " + ecoNumber + " already exists");
        }

        ECO eco = ECO.builder()
                .ecoNumber(ecoNumber)
                .title(ecr.getTitle())
                .description(ecr.getDescription())
                .status("DRAFT")
                .type("ECO")
                .ecr(ecr)
                .build();

        ECO savedEco = ecoRepository.save(eco);
        log.info("Converted ECR id: {} to ECO id: {}", ecrId, savedEco.getId());
        return ecoMapper.toDTO(savedEco);
    }
}
