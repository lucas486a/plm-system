package com.plm.workflow;

import com.plm.entity.ECR;
import com.plm.repository.ECRRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Flowable ExecutionListener that synchronizes ECR entity status with workflow state.
 * Attached to BPMN end events in the ecr-process to update ECR status upon
 * workflow completion (APPROVED or REJECTED).
 */
@Component("ecrWorkflowListener")
@RequiredArgsConstructor
@Slf4j
public class ECRWorkflowListener implements ExecutionListener {

    private final ECRRepository ecrRepository;

    @Override
    public void notify(DelegateExecution execution) {
        String activityId = execution.getCurrentActivityId();
        String processInstanceId = execution.getProcessInstanceId();
        String businessKey = execution.getProcessInstanceBusinessKey();

        log.info("ECR workflow listener triggered: activityId={}, processInstanceId={}, businessKey={}",
                activityId, processInstanceId, businessKey);

        // Extract ECR ID from business key (format: "ECR-{ecrId}")
        if (businessKey == null || !businessKey.startsWith("ECR-")) {
            log.warn("Invalid or missing business key for ECR workflow: {}", businessKey);
            return;
        }

        Long ecrId;
        try {
            ecrId = Long.parseLong(businessKey.substring(4));
        } catch (NumberFormatException e) {
            log.error("Cannot parse ECR ID from business key: {}", businessKey, e);
            return;
        }

        ECR ecr = ecrRepository.findById(ecrId).orElse(null);
        if (ecr == null) {
            log.error("ECR not found for id: {}", ecrId);
            return;
        }

        // Determine new status based on which end event was reached
        String newStatus = determineStatus(execution, activityId);
        if (newStatus != null) {
            String oldStatus = ecr.getStatus();
            ecr.setStatus(newStatus);
            ecrRepository.save(ecr);
            log.info("ECR {} status updated from {} to {} by workflow", ecrId, oldStatus, newStatus);
        }
    }

    /**
     * Determine the ECR status based on the execution context.
     * Checks the ecrStatus process variable set by BPMN service tasks,
     * or falls back to activity ID mapping.
     */
    private String determineStatus(DelegateExecution execution, String activityId) {
        // First try to get status from process variable (set by BPMN service tasks)
        Object ecrStatusVar = execution.getVariable("ecrStatus");
        if (ecrStatusVar instanceof String status) {
            return status;
        }

        // Fallback: map activity ID to status
        if ("endApproved".equals(activityId) || "setStatusApproved".equals(activityId)) {
            return "APPROVED";
        } else if ("endRejected".equals(activityId) || "setStatusRejected".equals(activityId)) {
            return "REJECTED";
        }

        log.warn("Could not determine ECR status for activityId: {}", activityId);
        return null;
    }
}
