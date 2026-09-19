package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.job.config.JobProperties;
import com.forgex.job.domain.entity.SysJobInstance;
import com.forgex.job.domain.param.JobInstanceMaintainParam;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.mapper.SysJobInstanceMapper;
import com.forgex.job.service.IJobInstanceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * Job 实例服务实现。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Service
@DS("job")
public class JobInstanceServiceImpl extends ServiceImpl<SysJobInstanceMapper, SysJobInstance> implements IJobInstanceService {

    /** Job 服务实例注册使用公共租户。 */
    private static final long PUBLIC_TENANT_ID = 0L;

    private final JobProperties properties;
    private final String serviceName;
    private final Integer port;
    private volatile String instanceId;

    public JobInstanceServiceImpl(JobProperties properties,
                                  @Value("${spring.application.name:forgex-job}") String serviceName,
                                  @Value("${server.port:9004}") Integer port) {
        this.properties = properties;
        this.serviceName = serviceName;
        this.port = port;
    }

    @Override
    public IPage<SysJobInstance> pageInstances(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getInstanceId()), SysJobInstance::getInstanceId, query.getInstanceId());
        wrapper.eq(query.getStatus() != null, SysJobInstance::getStatus, query.getStatus());
        wrapper.orderByDesc(SysJobInstance::getLastHeartbeatTime);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public String currentInstanceId() {
        if (instanceId == null) {
            instanceId = resolveIp() + ":" + port + ":" + resolvePid();
        }
        return instanceId;
    }

    /**
     * 注册或刷新当前执行器实例心跳。
     * <p>
     * 启动注册（ApplicationReadyEvent）与定时心跳（fixedDelay 首跑）可能并发触发，
     * 方法级同步保证同一进程内串行执行，避免"双方都查无记录后各自插入"撞唯一键
     * {@code uk_sys_job_instance(tenant_id, instance_id, deleted)}。
     */
    @Override
    public synchronized void registerOrHeartbeat() {
        String current = currentInstanceId();
        SysJobInstance entity = findByInstanceId(current);
        LocalDateTime now = LocalDateTime.now();
        // 分支一：无记录时首次注册
        if (entity == null) {
            SysJobInstance fresh = new SysJobInstance();
            // 实例注册由后台调度线程执行，没有请求租户上下文，显式归入公共租户。
            fresh.setTenantId(PUBLIC_TENANT_ID);
            fresh.setInstanceId(current);
            fresh.setServiceName(serviceName);
            fresh.setIp(resolveIp());
            fresh.setPort(port);
            fresh.setPid(resolvePid());
            fresh.setStatus(JobConstants.INSTANCE_ONLINE);
            fresh.setRunningCount(0);
            fresh.setStartTime(now);
            fresh.setLastHeartbeatTime(now);
            try {
                save(fresh);
                return;
            } catch (DuplicateKeyException ex) {
                // 兜底：注册与心跳极端并发或库中残留同实例记录时唯一键冲突，转为更新已有行
                entity = findByInstanceId(current);
                if (entity == null) {
                    throw ex;
                }
            }
        }
        // 分支二：已有记录时刷新心跳与状态
        entity.setLastHeartbeatTime(now);
        if (entity.getMaintenance() == null || entity.getMaintenance() == 0) {
            entity.setStatus(JobConstants.INSTANCE_ONLINE);
        } else {
            entity.setStatus(JobConstants.INSTANCE_MAINTENANCE);
        }
        updateById(entity);
    }

    @Override
    public void changeMaintenance(JobInstanceMaintainParam param) {
        SysJobInstance entity = param.getId() != null ? getById(param.getId()) : findByInstanceId(param.getInstanceId());
        if (entity == null) {
            return;
        }
        entity.setMaintenance(param.getMaintenance());
        entity.setStatus(param.getMaintenance() != null && param.getMaintenance() == 1
            ? JobConstants.INSTANCE_MAINTENANCE : JobConstants.INSTANCE_ONLINE);
        updateById(entity);
    }

    @Override
    public long countOnline() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(properties.getInstance().getOfflineThresholdSeconds());
        return count(new LambdaQueryWrapper<SysJobInstance>()
            .ge(SysJobInstance::getLastHeartbeatTime, threshold)
            .eq(SysJobInstance::getStatus, JobConstants.INSTANCE_ONLINE));
    }

    @Override
    public void increaseRunning() {
        changeRunning(1);
    }

    @Override
    public void decreaseRunning() {
        changeRunning(-1);
    }

    private void changeRunning(int delta) {
        SysJobInstance entity = findByInstanceId(currentInstanceId());
        if (entity == null) {
            return;
        }
        int count = entity.getRunningCount() == null ? 0 : entity.getRunningCount();
        entity.setRunningCount(Math.max(0, count + delta));
        updateById(entity);
    }

    private SysJobInstance findByInstanceId(String instanceId) {
        return getOne(new LambdaQueryWrapper<SysJobInstance>()
            .eq(SysJobInstance::getInstanceId, instanceId), false);
    }

    private String resolveIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "127.0.0.1";
        }
    }

    private String resolvePid() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int index = runtimeName.indexOf('@');
        return index > 0 ? runtimeName.substring(0, index) : runtimeName;
    }
}
