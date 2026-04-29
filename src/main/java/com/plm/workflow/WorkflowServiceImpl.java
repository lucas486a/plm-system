package com.plm.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Implementation of WorkflowService using Flowable engine APIs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkflowServiceImpl implements WorkflowService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    @Override
    @Transactional
    public String startProcess(String processKey, Map<String, Object> variables) {
        log.info("Starting process with key: {}, variables: {}", processKey, variables);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, variables);
        log.info("Started process instance: {}", processInstance.getId());
        return processInstance.getId();
    }

    @Override
    @Transactional
    public String startProcess(String processKey, String businessKey, Map<String, Object> variables) {
        log.info("Starting process with key: {}, businessKey: {}, variables: {}", processKey, businessKey, variables);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
        log.info("Started process instance: {} with business key: {}", processInstance.getId(), businessKey);
        return processInstance.getId();
    }

    @Override
    @Transactional
    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("Completing task: {}, variables: {}", taskId, variables);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        taskService.complete(taskId, variables);
        log.info("Completed task: {}", taskId);
    }

    @Override
    public List<Task> getTasksByAssignee(String assignee) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .list();
    }

    @Override
    public List<Task> getTasksByProcessInstance(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
    }

    @Override
    public ProcessInstanceDTO getProcessInstance(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            // Check history for completed processes
            var historicProcess = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (historicProcess == null) {
                return null;
            }

            return ProcessInstanceDTO.builder()
                    .processInstanceId(historicProcess.getId())
                    .processDefinitionId(historicProcess.getProcessDefinitionId())
                    .processDefinitionKey(historicProcess.getProcessDefinitionKey())
                    .businessKey(historicProcess.getBusinessKey())
                    .ended(historicProcess.getEndTime() != null)
                    .startTime(historicProcess.getStartTime() != null ? historicProcess.getStartTime().toInstant() : null)
                    .build();
        }

        return ProcessInstanceDTO.builder()
                .processInstanceId(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .processDefinitionKey(processInstance.getProcessDefinitionKey())
                .businessKey(processInstance.getBusinessKey())
                .suspended(processInstance.isSuspended())
                .ended(processInstance.isEnded())
                .build();
    }

    @Override
    public boolean isProcessActive(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return processInstance != null && !processInstance.isEnded();
    }

    @Override
    @Transactional
    public void deleteProcessInstance(String processInstanceId, String reason) {
        log.info("Deleting process instance: {}, reason: {}", processInstanceId, reason);
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Deleted process instance: {}", processInstanceId);
    }

    @Override
    public String findProcessInstanceByBusinessKey(String businessKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .singleResult();
        return processInstance != null ? processInstance.getId() : null;
    }
}
