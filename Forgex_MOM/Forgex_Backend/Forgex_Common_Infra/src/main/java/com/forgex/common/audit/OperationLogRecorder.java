package com.forgex.common.audit;

/**
 * 操作日志记录器接口
 * <p>
 * 定义操作日志记录的标准接口，用于将{@link OperationLogRecord}持久化存储。
 * </p>
 * <p><strong>实现说明：</strong></p>
 * <ul>
 *   <li>实现类需要将操作日志记录保存到数据库、文件或其他存储介质</li>
 *   <li>可以通过Spring的{@code @Autowired}或{@code @Resource}注入实现类</li>
 *   <li>支持异步记录以提高性能</li>
 * </ul>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * @Service
 * public class DatabaseOperationLogRecorder implements OperationLogRecorder {
 *     private final OperationLogMapper mapper;
 *     
 *     @Override
 *     public void record(OperationLogRecord record) {
 *         mapper.insert(record);
 *     }
 * }
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see OperationLogRecord
 * @see OperationLogAspect
 */
public interface OperationLogRecorder {
    /**
     * 记录操作日志
     * <p>
     * 将操作日志记录持久化存储到数据库、文件或其他存储介质。
     * </p>
     * 
     * @param record 操作日志记录对象，包含完整的操作信息
     */
    void record(OperationLogRecord record);
}

