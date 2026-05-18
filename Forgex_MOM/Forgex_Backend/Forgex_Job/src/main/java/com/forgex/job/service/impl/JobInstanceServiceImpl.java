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

    @Override
    public void registerOrHeartbeat() {
        String current = currentInstanceId();
        SysJobInstance entity = getOne(new LambdaQueryWrapper<SysJobInstance>()
            .eq(SysJobInstance::getInstanceId, current), false);
        LocalDateTime now = LocalDateTime.now();
        if (entity == null) {
            entity = new SysJobInstance();
            entity.setInstanceId(current);
            entity.setServiceName(serviceName);
            entity.setIp(resolveIp());
            entity.setPort(port);
            entity.setPid(resolvePid());
            entity.setStatus(JobConstants.INSTANCE_ONLINE);
            entity.setRunningCount(0);
            entity.setStartTime(now);
        }
        entity.setLastHeartbeatTime(now);
        if (entity.getMaintenance() == null || entity.getMaintenance() == 0) {
            entity.setStatus(JobConstants.INSTANCE_ONLINE);
        } else {
            entity.setStatus(JobConstants.INSTANCE_MAINTENANCE);
        }
        saveOrUpdate(entity);
    }

    @Override
    public void changeMaintenance(JobInstanceMaintainParam param) {
        SysJobInstance entity = param.getId() != null ? getById(param.getId())
            : getOne(new LambdaQueryWrapper<SysJobInstance>().eq(SysJobInstance::getInstanceId, param.getInstanceId()), false);
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
        SysJobInstance entity = getOne(new LambdaQueryWrapper<SysJobInstance>()
            .eq(SysJobInstance::getInstanceId, currentInstanceId()), false);
        if (entity == null) {
            return;
        }
        int count = entity.getRunningCount() == null ? 0 : entity.getRunningCount();
        entity.setRunningCount(Math.max(0, count + delta));
        updateById(entity);
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
