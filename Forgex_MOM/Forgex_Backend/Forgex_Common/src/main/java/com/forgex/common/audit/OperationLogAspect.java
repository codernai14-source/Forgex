package com.forgex.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面
 * <p>
 * 基于Spring AOP实现的操作日志记录切面，用于拦截带有@OperationLog注解的方法，
 * 自动记录操作日志信息，包括请求参数、响应结果、执行时间、异常信息等。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>拦截带有@OperationLog注解的方法</li>
 *   <li>记录操作基本信息（租户、用户、模块、菜单路径）</li>
 *   <li>记录HTTP请求信息（请求方法、URL、IP、User-Agent）</li>
 *   <li>记录请求参数和响应结果</li>
 *   <li>记录执行时间和异常堆栈</li>
 *   <li>支持操作详情模板和字段提取</li>
 *   <li>自动过滤敏感对象（HttpServletRequest、HttpServletResponse、MultipartFile）</li>
 * </ul>
 * <p><strong>日志记录流程：</strong></p>
 * <ol>
 *   <li>方法执行前记录开始时间</li>
 *   <li>执行目标方法</li>
 *   <li>方法执行成功或失败后，记录操作日志</li>
 *   <li>构建OperationLogRecord对象</li>
 *   <li>调用OperationLogRecorder记录日志</li>
 * </ol>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see OperationLog
 * @see OperationLogRecord
 * @see OperationLogRecorder
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper;

    /**
     * 操作日志记录器提供者
     */
    private final ObjectProvider<OperationLogRecorder> recorderProvider;

    /**
     * 环绕通知
     * <p>
     * 拦截带有@OperationLog注解的方法，记录操作日志。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>记录方法开始时间</li>
     *   <li>执行目标方法</li>
     *   <li>如果方法执行成功，记录结果</li>
     *   <li>如果方法执行失败，记录异常</li>
     *   <li>计算执行时间</li>
     *   <li>调用record方法记录日志</li>
     *   <li>返回方法执行结果或抛出异常</li>
     * </ol>
     * 
     * @param pjp 切点连接点
     * @param op 操作日志注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(op)")
    public Object around(ProceedingJoinPoint pjp, OperationLog op) throws Throwable {
        // 记录方法开始时间
        long start = System.currentTimeMillis();
        
        // 初始化异常和结果变量
        Throwable error = null;
        Object result = null;
        
        try {
            // 执行目标方法
            result = pjp.proceed();
            
            // 返回方法执行结果
            return result;
        } catch (Throwable t) {
            // 记录异常
            error = t;
            
            // 抛出异常
            throw t;
        } finally {
            try {
                // 计算执行时间
                long cost = System.currentTimeMillis() - start;
                
                // 调用record方法记录日志
                record(pjp, op, result, error, cost);
            } catch (Exception ignored) {
                // 记录日志失败，忽略异常
            }
        }
    }

    /**
     * 记录操作日志
     * <p>
     * 构建OperationLogRecord对象，并调用OperationLogRecorder记录日志。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>获取OperationLogRecorder</li>
     *   <li>如果recorder为空，直接返回</li>
     *   <li>构建OperationLogRecord对象</li>
     *   <li>设置租户ID和用户ID</li>
     *   <li>设置模块、菜单路径、操作类型</li>
     *   <li>设置操作时间和执行时间</li>
     *   <li>获取当前HTTP请求</li>
     *   <li>设置请求方法、URL、IP、User-Agent</li>
     *   <li>序列化请求参数</li>
     *   <li>序列化响应结果</li>
     *   <li>如果发生异常，设置异常堆栈</li>
     *   <li>设置详情模板和详情字段</li>
     *   <li>调用recorder记录日志</li>
     * </ol>
     * 
     * @param pjp 切点连接点
     * @param op 操作日志注解
     * @param result 方法执行结果
     * @param error 方法执行异常
     * @param cost 执行时间（毫秒）
     */
    private void record(ProceedingJoinPoint pjp, OperationLog op, Object result, Throwable error, long cost) {
        // 获取OperationLogRecorder
        OperationLogRecorder recorder = recorderProvider == null ? null : recorderProvider.getIfAvailable();
        
        // 如果recorder为空，直接返回
        if (recorder == null || op == null) {
            return;
        }

        // 构建OperationLogRecord对象
        OperationLogRecord r = new OperationLogRecord();
        
        // 设置租户ID和用户ID
        r.setTenantId(TenantContext.get());
        r.setUserId(UserContext.get());
        
        // 设置模块、菜单路径、操作类型
        r.setModule(op.module());
        r.setMenuPath(op.menuPath());
        r.setOperationType(op.operationType() == null ? null : op.operationType().name());
        
        // 设置操作时间和执行时间
        r.setOperationTime(LocalDateTime.now());
        r.setCostTime(cost);

        // 获取当前HTTP请求
        HttpServletRequest request = currentRequest();
        if (request != null) {
            // 设置请求方法、URL、IP、User-Agent
            r.setRequestMethod(request.getMethod());
            r.setRequestUrl(request.getRequestURI());
            r.setIp(resolveIp(request));
            r.setUserAgent(request.getHeader("User-Agent"));
            
            // 从请求头获取账号
            String account = request.getHeader("X-Account");
            if (StringUtils.hasText(account)) {
                r.setUsername(account);
            }
        }

        // 序列化请求参数
        r.setRequestParams(safeWriteArgs(pjp == null ? null : pjp.getArgs()));

        // 序列化响应结果
        if (result instanceof R<?> rr) {
            // 如果结果是R类型，提取data字段
            r.setResponseStatus(rr.getCode());
            r.setResponseResult(safeWrite(rr.getData()));
        } else if (result != null) {
            // 如果结果不是R类型，直接序列化
            r.setResponseResult(safeWrite(result));
        }

        // 如果发生异常，设置异常堆栈
        if (error != null) {
            // 设置响应状态码
            r.setResponseStatus(r.getResponseStatus() == null ? 500 : r.getResponseStatus());
            
            // 设置异常堆栈
            r.setErrorStack(stack(error));
        }

        // 设置详情模板
        if (StringUtils.hasText(op.detailTemplateCode())) {
            r.setDetailTemplateCode(op.detailTemplateCode());
        }
        
        // 设置详情字段
        if (op.detailFields() != null && op.detailFields().length > 0) {
            r.setDetailFields(resolveDetailFields(op.detailFields(), result, pjp == null ? null : pjp.getArgs()));
        }

        // 调用recorder记录日志
        recorder.record(r);
    }

    /**
     * 解析详情字段
     * <p>
     * 从方法结果或参数中提取指定字段的值。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>解包结果对象（如果是R类型，提取data字段）</li>
     *   <li>遍历字段列表</li>
     *   <li>尝试从结果对象中读取字段值</li>
     *   <li>如果结果对象中没有，尝试从参数中读取</li>
     *   <li>将字段名和值添加到Map中</li>
     * </ol>
     * 
     * @param fields 字段名数组
     * @param result 方法执行结果
     * @param args 方法参数数组
     * @return 字段名到字段值的映射
     */
    private Map<String, Object> resolveDetailFields(String[] fields, Object result, Object[] args) {
        // 解包结果对象
        Object root = unwrapResult(result);
        
        // 初始化字段映射
        Map<String, Object> map = new LinkedHashMap<>();
        
        // 遍历字段列表
        for (String f : fields) {
            // 跳过空字段
            if (!StringUtils.hasText(f)) continue;
            
            // 尝试从结果对象中读取字段值
            Object v = readPath(root, f);
            
            // 如果结果对象中没有，尝试从参数中读取
            if (v == null && args != null) {
                for (Object a : args) {
                    v = readPath(a, f);
                    if (v != null) break;
                }
            }
            
            // 将字段名和值添加到Map中
            map.put(f, v);
        }
        
        return map;
    }

    /**
     * 解包结果对象
     * <p>
     * 如果结果是R类型，提取data字段；否则返回原对象。
     * </p>
     * 
     * @param result 方法执行结果
     * @return 解包后的对象
     */
    private Object unwrapResult(Object result) {
        // 如果结果是R类型，提取data字段
        if (result instanceof R<?> rr) {
            return rr.getData();
        }
        
        // 否则返回原对象
        return result;
    }

    /**
     * 读取对象路径值
     * <p>
     * 从对象中读取指定路径的属性值。
     * 支持Map类型和JavaBean对象。
     * </p>
     * 
     * @param obj 对象实例
     * @param path 属性路径
     * @return 属性值，读取失败返回null
     */
    private Object readPath(Object obj, String path) {
        // 参数校验
        if (obj == null || !StringUtils.hasText(path)) {
            return null;
        }
        
        // 如果是Map类型，直接通过键获取
        if (obj instanceof Map<?, ?> m) {
            return m.get(path);
        }
        
        // 如果是JavaBean对象，通过BeanWrapper读取
        try {
            BeanWrapper bw = new BeanWrapperImpl(obj);
            if (bw.isReadableProperty(path)) {
                return bw.getPropertyValue(path);
            }
        } catch (Exception ignored) {
            // 读取失败，忽略异常
        }
        
        return null;
    }

    /**
     * 安全序列化参数数组
     * <p>
     * 过滤掉HttpServletRequest、HttpServletResponse、MultipartFile等敏感对象，
     * 然后序列化剩余参数。
     * </p>
     * 
     * @param args 参数数组
     * @return 序列化后的JSON字符串
     */
    private String safeWriteArgs(Object[] args) {
        // 参数为空或长度为0，返回null
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 过滤敏感对象
        Object[] filtered = Arrays.stream(args)
                .filter(a -> a != null)
                .filter(a -> !(a instanceof HttpServletRequest))
                .filter(a -> !(a instanceof jakarta.servlet.http.HttpServletResponse))
                .filter(a -> !(a instanceof MultipartFile))
                .toArray();
        
        // 序列化过滤后的参数
        return safeWrite(filtered);
    }

    /**
     * 安全序列化对象
     * <p>
     * 将对象序列化为JSON字符串，如果序列化失败则返回对象的toString结果。
     * 限制最大长度为8000字符。
     * </p>
     * 
     * @param obj 要序列化的对象
     * @return 序列化后的字符串
     */
    private String safeWrite(Object obj) {
        // 对象为空，返回null
        if (obj == null) {
            return null;
        }
        
        try {
            // 序列化为JSON字符串
            String s = objectMapper.writeValueAsString(obj);
            
            // 限制最大长度为8000字符
            if (s.length() > 8000) {
                return s.substring(0, 8000);
            }
            
            return s;
        } catch (Exception e) {
            // 序列化失败，返回toString结果
            return String.valueOf(obj);
        }
    }

    /**
     * 获取异常堆栈信息
     * <p>
     * 将异常转换为字符串，限制最大长度为2000字符。
     * </p>
     * 
     * @param t 异常对象
     * @return 异常堆栈字符串
     */
    private String stack(Throwable t) {
        // 异常为空，返回null
        if (t == null) return null;
        
        // 获取异常字符串
        String s = t.toString();
        
        // 限制最大长度为2000字符
        if (s.length() > 2000) {
            return s.substring(0, 2000);
        }
        
        return s;
    }

    /**
     * 获取当前HTTP请求
     * <p>
     * 从RequestContextHolder中获取当前请求对象。
     * </p>
     * 
     * @return 当前HTTP请求对象，获取失败返回null
     */
    private HttpServletRequest currentRequest() {
        try {
            // 从RequestContextHolder中获取请求属性
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                return attrs.getRequest();
            }
        } catch (Exception ignored) {
            // 获取失败，忽略异常
        }
        
        return null;
    }

    /**
     * 解析客户端IP地址
     * <p>
     * 优先从X-Client-IP请求头获取，如果不存在则使用RemoteAddr。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String resolveIp(HttpServletRequest request) {
        // 尝试从X-Client-IP请求头获取
        String ip = request.getHeader("X-Client-IP");
        
        // 如果存在，直接返回
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        
        // 否则使用RemoteAddr
        return request.getRemoteAddr();
    }
}
