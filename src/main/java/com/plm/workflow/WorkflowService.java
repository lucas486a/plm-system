package com.plm.workflow;

import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Flowable workflow operations.
 * Provides methods to start processes, manage tasks, and query process instances.
 */
public interface WorkflowService {

    /**
     * Start a new process instance by process definition key.
     *
     * @param processKey the process definition key (e.g., "ecr-process", "eco-process")
     * @param variables  process variables to set on start
     * @return the process instance ID
     */
    String startProcess(String processKey, Map<String, Object> variables);

    /**
     * Start a new process instance with a business key for entity correlation.
     *
     * @param processKey the process definition key
     * @param businessKey the business key (e.g., "ECR-123")
     * @param variables  process variables to set on start
     * @return the process instance ID
     */
    String startProcess(String processKey, String businessKey, Map<String, Object> variables);

    /**
     * Complete a user task with optional variables.
     *
     * @param taskId    the task ID to complete
     * @param variables task variables (e.g., "approved" = true/false)
     */
    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * Get all tasks assigned to a specific user.
     *
     * @param assignee the assignee username
     * @return list of tasks assigned to the user
     */
    List<Task> getTasksByAssignee(String assignee);

    /**
     * Get tasks for a specific process instance.
     *
     * @param processInstanceId the process instance ID
     * @return list of active tasks in the process
     */
    List<Task> getTasksByProcessInstance(String processInstanceId);

    /**
     * Get the current state of a process instance.
     *
     * @param processInstanceId the process instance ID
     * @return ProcessInstanceDTO with process details, or null if not found
     */
    ProcessInstanceDTO getProcessInstance(String processInstanceId);

    /**
     * Check if a process instance is still active (running).
     *
     * @param processInstanceId the process instance ID
     * @return true if the process is active
     */
    boolean isProcessActive(String processInstanceId);

    /**
     * Delete a process instance (cancel the workflow).
     *
     * @param processInstanceId the process instance ID
     * @param reason            reason for deletion
     */
    void deleteProcessInstance(String processInstanceId, String reason);

    /**
     * Find active process instance ID by business key.
     *
     * @param businessKey the business key (e.g., "ECR-123")
     * @return the process instance ID, or null if not found
     */
    String findProcessInstanceByBusinessKey(String businessKey);
}
