package com.forgex.job.core.executor;

import com.forgex.job.domain.entity.SysJobTask;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Java Bean 任务调用器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class JavaBeanJobInvoker {

    private final ApplicationContext applicationContext;

    public JavaBeanJobInvoker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public JobResult invoke(SysJobTask task, JobExecutionContext context) throws Exception {
        if (!StringUtils.hasText(task.getBeanName())) {
            throw new IllegalArgumentException("beanName required");
        }
        Object bean = applicationContext.getBean(task.getBeanName());
        String methodName = StringUtils.hasText(task.getMethodName()) ? task.getMethodName() : "execute";
        Method method = bean.getClass().getMethod(methodName, JobExecutionContext.class);
        Object result = method.invoke(bean, context);
        if (result instanceof JobResult jobResult) {
            return jobResult;
        }
        return JobResult.success("success");
    }
}
