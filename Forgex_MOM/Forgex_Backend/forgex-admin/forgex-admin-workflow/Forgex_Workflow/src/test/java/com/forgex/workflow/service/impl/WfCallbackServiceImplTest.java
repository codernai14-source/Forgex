package com.forgex.workflow.service.impl;

import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.service.callback.WorkflowCallbackHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WfCallbackServiceImplTest {

    @Test
    void triggerCallbackShouldDispatchConfiguredLocalBeanWhenUrlIsBlank() {
        WfTaskExecutionMapper executionMapper = mock(WfTaskExecutionMapper.class);
        WfTaskConfigMapper taskConfigMapper = mock(WfTaskConfigMapper.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        CapturingCallbackHandler handler = new CapturingCallbackHandler();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getDefaultListableBeanFactory().registerSingleton("approvalCallbackHandler", handler);

        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(100L);
        execution.setTaskConfigId(200L);
        execution.setFormContent("{\"amount\":10}");
        when(executionMapper.selectById(100L)).thenReturn(execution);

        WfTaskConfig taskConfig = new WfTaskConfig();
        taskConfig.setId(200L);
        taskConfig.setTaskCode("SUPPLIER_AUDIT");
        taskConfig.setTaskName("供应商准入");
        taskConfig.setCallbackBean("approvalCallbackHandler");
        when(taskConfigMapper.selectById(200L)).thenReturn(taskConfig);

        WfCallbackServiceImpl service = new WfCallbackServiceImpl(
                executionMapper,
                taskConfigMapper,
                restTemplate,
                applicationContext);

        service.triggerCallback(100L, 2);

        verifyNoInteractions(restTemplate);
        assertEquals(100L, handler.payload.get("executionId"));
        assertEquals("SUPPLIER_AUDIT", handler.payload.get("taskCode"));
        assertEquals("供应商准入", handler.payload.get("taskName"));
        assertEquals(2, handler.payload.get("status"));
        assertEquals("{\"amount\":10}", handler.payload.get("formContent"));
    }

    private static class CapturingCallbackHandler implements WorkflowCallbackHandler {
        private Map<String, Object> payload;

        @Override
        public void handle(Map<String, Object> payload) {
            this.payload = payload;
        }
    }
}
