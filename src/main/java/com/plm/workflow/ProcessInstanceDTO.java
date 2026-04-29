package com.plm.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO representing a Flowable process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInstanceDTO {

    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String businessKey;
    private boolean suspended;
    private boolean ended;
    private Instant startTime;
    private Map<String, Object> variables;
}
