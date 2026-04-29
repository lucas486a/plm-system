package com.plm.workflow;

import com.plm.entity.ECO;
import com.plm.repository.ECORepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Flowable execution listener for ECO approval workflow events.
 * Triggered when the BPMN process reaches an end event.
 * Synchronizes ECO status with workflow state.
 *
 * End events in eco-approval.bpmn20.xml:
 * - "endApproved": All stages approved → ECO status = APPROVED
 * - "endRejected": Rejection at any stage → ECO status = DRAFT
 */
@Slf4j
@Component("ecoWorkflowListener")
@RequiredArgsConstructor
public class ECOWorkflowListener implements ExecutionListener {

    private final ECORepository ecoRepository;

    private static final String END_APPROVED = "endApproved";
    private static final String END_REJECTED = "endRejected";

    @Override
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String activityId = execution.getCurrentActivityId();
        Long ecoId = getEcoId(execution);

        log.info("ECO workflow listener triggered: processInstance={}, activityId={}, ecoId={}",
                processInstanceId, activityId, ecoId);

        if (ecoId == null) {
            log.error("No ecoId found in process variables for process instance: {}", processInstanceId);
            return;
        }

        ECO eco = ecoRepository.findById(ecoId).orElse(null);
        if (eco == null) {
            log.error("ECO not found with id: {} for process instance: {}", ecoId, processInstanceId);
            return;
        }

        switch (activityId != null ? activityId : "") {
            case END_APPROVED -> handleApproved(eco, processInstanceId);
            case END_REJECTED -> handleRejected(eco, processInstanceId);
            default -> log.warn("Unknown end activity '{}' for ECO id: {}", activityId, ecoId);
        }
    }

    /**
     * All approval stages completed successfully.
     * Transition: IN_PROGRESS → APPROVED
     */
    private void handleApproved(ECO eco, String processInstanceId) {
        log.info("ECO id: {} fully approved via workflow process: {}", eco.getId(), processInstanceId);

        if (!"IN_PROGRESS".equals(eco.getStatus())) {
            log.warn("ECO id: {} is in status '{}', expected IN_PROGRESS. Updating to APPROVED anyway.",
                    eco.getId(), eco.getStatus());
        }

        eco.setStatus("APPROVED");
        eco.setCurrentStage(null);
        ecoRepository.save(eco);

        log.info("ECO id: {} status updated to APPROVED", eco.getId());
    }

    /**
     * Rejection at any approval stage.
     * Transition: IN_PROGRESS → DRAFT
     */
    private void handleRejected(ECO eco, String processInstanceId) {
        log.info("ECO id: {} rejected via workflow process: {}", eco.getId(), processInstanceId);

        eco.setStatus("DRAFT");
        eco.setCurrentStage(null);
        ecoRepository.save(eco);

        log.info("ECO id: {} status updated to DRAFT (rejected)", eco.getId());
    }

    /**
     * Extract ecoId from process variables.
     * The ecoId is set as a process variable when the workflow is started.
     */
    private Long getEcoId(DelegateExecution execution) {
        Object ecoId = execution.getVariable("ecoId");
        if (ecoId instanceof Long) {
            return (Long) ecoId;
        }
        if (ecoId instanceof Number) {
            return ((Number) ecoId).longValue();
        }
        if (ecoId instanceof String) {
            try {
                return Long.parseLong((String) ecoId);
            } catch (NumberFormatException e) {
                log.error("Invalid ecoId format in process variables: {}", ecoId);
                return null;
            }
        }
        return null;
    }
}
