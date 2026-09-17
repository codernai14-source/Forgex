package com.forgex.common.audit;

/**
 * 本地操作日志记录器标记。
 * <p>
 * Sys 模块注册该 Bean 后，远程兜底记录器不再生效，避免重复入库。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SysLocalOperationLogRecorderMarker {
}
