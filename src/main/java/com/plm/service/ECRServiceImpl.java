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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ECRServiceImpl implements ECRService {

    private final ECRRepository ecrRepository;
    private final ECRPartRepository ecrPartRepository;
    private final UserRepository userRepository;
    private final PartRepository partRepository;
    private final ECRMapper ecrMapper;
    private final PartMapper partMapper;
    private final WorkflowService workflowService;

    private static final String ECR_PROCESS_KEY = "ecr-process";
    private static final String BUSINESS_KEY_PREFIX = "ECR-";

    // =========================================================================
    // CRUD Operations
    // =========================================================================

    @Override
    @Transactional
    public ECRDTO createECR(ECRDTO ecrDTO) {
        if (ecrRepository.existsByEcrNumber(ecrDTO.getEcrNumber())) {
            throw new IllegalArgumentException("ECR number already exists: " + ecrDTO.getEcrNumber());
        }

        ECR ecr = ecrMapper.toEntity(ecrDTO);
        ecr.setStatus("DRAFT");
        ecr.setIsDeleted(false);

        // Handle assigned user
        if (ecrDTO.getAssignedToId() != null) {
            User user = userRepository.findById(ecrDTO.getAssignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + ecrDTO.getAssignedToId()));
            ecr.setAssignedTo(user);
        }

        ECR saved = ecrRepository.save(ecr);
        return ecrMapper.toDTO(saved);
    }

    @Override
    public ECRDTO getECRById(Long id) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));
        return ecrMapper.toDTO(ecr);
    }

    @Override
    public ECRDTO getECRByNumber(String ecrNumber) {
        ECR ecr = ecrRepository.findByEcrNumber(ecrNumber)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + ecrNumber));
        return ecrMapper.toDTO(ecr);
    }

    @Override
    @Transactional
    public ECRDTO updateECR(Long id, ECRDTO ecrDTO) {
        ECR existing = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        // Only allow updates in DRAFT status
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new IllegalStateException("Cannot update ECR in status: " + existing.getStatus());
        }

        // Update fields
        if (ecrDTO.getTitle() != null) {
            existing.setTitle(ecrDTO.getTitle());
        }
        if (ecrDTO.getDescription() != null) {
            existing.setDescription(ecrDTO.getDescription());
        }
        if (ecrDTO.getPriority() != null) {
            existing.setPriority(ecrDTO.getPriority());
        }

        // Handle assigned user update
        if (ecrDTO.getAssignedToId() != null) {
            User user = userRepository.findById(ecrDTO.getAssignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + ecrDTO.getAssignedToId()));
            existing.setAssignedTo(user);
        }

        ECR saved = ecrRepository.save(existing);
        return ecrMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteECR(Long id) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        // Only allow deletion in DRAFT status
        if (!"DRAFT".equals(ecr.getStatus())) {
            throw new IllegalStateException("Cannot delete ECR in status: " + ecr.getStatus());
        }

        ecrRepository.delete(ecr);
    }

    @Override
    public Page<ECRDTO> listECRs(Pageable pageable) {
        return ecrRepository.findAll(pageable)
                .map(ecrMapper::toDTO);
    }

    // =========================================================================
    // Status Management (State Machine)
    // =========================================================================

    @Override
    @Transactional
    public ECRDTO submitECR(Long id) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        if (!"DRAFT".equals(ecr.getStatus())) {
            throw new IllegalStateException("Cannot submit ECR in status: " + ecr.getStatus());
        }

        ecr.setStatus("SUBMITTED");
        ECR saved = ecrRepository.save(ecr);

        // Start workflow process with business key for correlation
        String businessKey = BUSINESS_KEY_PREFIX + id;
        Map<String, Object> variables = new HashMap<>();
        variables.put("ecrId", id);
        variables.put("ecrNumber", ecr.getEcrNumber());
        variables.put("ecrStatus", "SUBMITTED");
        if (ecr.getAssignedTo() != null) {
            variables.put("evaluator", ecr.getAssignedTo().getUsername());
            variables.put("approver", ecr.getAssignedTo().getUsername());
        }

        String processInstanceId = workflowService.startProcess(ECR_PROCESS_KEY, businessKey, variables);
        log.info("Started ECR workflow process {} for ECR {}", processInstanceId, id);

        return ecrMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ECRDTO evaluateECR(Long id) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        if (!"SUBMITTED".equals(ecr.getStatus())) {
            throw new IllegalStateException("Cannot evaluate ECR in status: " + ecr.getStatus());
        }

        // Complete the evaluate task in the workflow
        String businessKey = BUSINESS_KEY_PREFIX + id;
        String processInstanceId = workflowService.findProcessInstanceByBusinessKey(businessKey);
        if (processInstanceId != null) {
            List<Task> tasks = workflowService.getTasksByProcessInstance(processInstanceId);
            tasks.stream()
                    .filter(task -> "Evaluate ECR".equals(task.getName()))
                    .findFirst()
                    .ifPresent(task -> {
                        Map<String, Object> variables = Map.of("ecrStatus", "EVALUATED");
                        workflowService.completeTask(task.getId(), variables);
                        log.info("Completed evaluate task for ECR {}", id);
                    });
        }

        ecr.setStatus("EVALUATED");
        ECR saved = ecrRepository.save(ecr);
        return ecrMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ECRDTO approveECR(Long id) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        if (!"EVALUATED".equals(ecr.getStatus())) {
            throw new IllegalStateException("Cannot approve ECR in status: " + ecr.getStatus());
        }

        // Complete the approve/reject task in the workflow with approved=true
        String businessKey = BUSINESS_KEY_PREFIX + id;
        String processInstanceId = workflowService.findProcessInstanceByBusinessKey(businessKey);
        if (processInstanceId != null) {
            List<Task> tasks = workflowService.getTasksByProcessInstance(processInstanceId);
            tasks.stream()
                    .filter(task -> "Approve or Reject ECR".equals(task.getName()))
                    .findFirst()
                    .ifPresent(task -> {
                        Map<String, Object> variables = Map.of("approved", true, "ecrStatus", "APPROVED");
                        workflowService.completeTask(task.getId(), variables);
                        log.info("Completed approve task for ECR {}", id);
                    });
        }

        ecr.setStatus("APPROVED");
        ECR saved = ecrRepository.save(ecr);
        return ecrMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ECRDTO rejectECR(Long id) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        if (!"EVALUATED".equals(ecr.getStatus())) {
            throw new IllegalStateException("Cannot reject ECR in status: " + ecr.getStatus());
        }

        // Complete the approve/reject task in the workflow with approved=false
        String businessKey = BUSINESS_KEY_PREFIX + id;
        String processInstanceId = workflowService.findProcessInstanceByBusinessKey(businessKey);
        if (processInstanceId != null) {
            List<Task> tasks = workflowService.getTasksByProcessInstance(processInstanceId);
            tasks.stream()
                    .filter(task -> "Approve or Reject ECR".equals(task.getName()))
                    .findFirst()
                    .ifPresent(task -> {
                        Map<String, Object> variables = Map.of("approved", false, "ecrStatus", "REJECTED");
                        workflowService.completeTask(task.getId(), variables);
                        log.info("Completed reject task for ECR {}", id);
                    });
        }

        ecr.setStatus("REJECTED");
        ECR saved = ecrRepository.save(ecr);
        return ecrMapper.toDTO(saved);
    }

    // =========================================================================
    // Assignment
    // =========================================================================

    @Override
    @Transactional
    public ECRDTO assignECR(Long id, Long userId) {
        ECR ecr = ecrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ecr.setAssignedTo(user);
        ECR saved = ecrRepository.save(ecr);
        return ecrMapper.toDTO(saved);
    }

    // =========================================================================
    // Impact Analysis (Affected Parts)
    // =========================================================================

    @Override
    public List<PartDTO> getAffectedParts(Long ecrId) {
        // Verify ECR exists
        if (!ecrRepository.existsById(ecrId)) {
            throw new IllegalArgumentException("ECR not found: " + ecrId);
        }

        return ecrPartRepository.findByEcrId(ecrId).stream()
                .map(ecrPart -> partMapper.toDto(ecrPart.getPart()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PartDTO addAffectedPart(Long ecrId, Long partId) {
        ECR ecr = ecrRepository.findById(ecrId)
                .orElseThrow(() -> new IllegalArgumentException("ECR not found: " + ecrId));

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found: " + partId));

        // Check if already exists
        if (ecrPartRepository.existsByEcrIdAndPartId(ecrId, partId)) {
            throw new IllegalStateException("Part already affected by this ECR");
        }

        ECRPart ecrPart = ECRPart.builder()
                .ecr(ecr)
                .part(part)
                .build();

        ecrPartRepository.save(ecrPart);
        return partMapper.toDto(part);
    }

    @Override
    @Transactional
    public void removeAffectedPart(Long ecrId, Long partId) {
        if (!ecrRepository.existsById(ecrId)) {
            throw new IllegalArgumentException("ECR not found: " + ecrId);
        }

        if (!ecrPartRepository.existsByEcrIdAndPartId(ecrId, partId)) {
            throw new IllegalArgumentException("Part not affected by this ECR");
        }

        ecrPartRepository.deleteByEcrIdAndPartId(ecrId, partId);
    }
}
