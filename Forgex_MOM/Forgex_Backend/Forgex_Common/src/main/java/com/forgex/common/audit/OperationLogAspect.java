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

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectMapper objectMapper;
    private final ObjectProvider<OperationLogRecorder> recorderProvider;

    @Around("@annotation(op)")
    public Object around(ProceedingJoinPoint pjp, OperationLog op) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        Object result = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                record(pjp, op, result, error, System.currentTimeMillis() - start);
            } catch (Exception ignored) {
            }
        }
    }

    private void record(ProceedingJoinPoint pjp, OperationLog op, Object result, Throwable error, long cost) {
        OperationLogRecorder recorder = recorderProvider == null ? null : recorderProvider.getIfAvailable();
        if (recorder == null || op == null) {
            return;
        }

        OperationLogRecord r = new OperationLogRecord();
        r.setTenantId(TenantContext.get());
        r.setUserId(UserContext.get());
        r.setModule(op.module());
        r.setMenuPath(op.menuPath());
        r.setOperationType(op.operationType() == null ? null : op.operationType().name());
        r.setOperationTime(LocalDateTime.now());
        r.setCostTime(cost);

        HttpServletRequest request = currentRequest();
        if (request != null) {
            r.setRequestMethod(request.getMethod());
            r.setRequestUrl(request.getRequestURI());
            r.setIp(resolveIp(request));
            r.setUserAgent(request.getHeader("User-Agent"));
            String account = request.getHeader("X-Account");
            if (StringUtils.hasText(account)) {
                r.setUsername(account);
            }
        }

        r.setRequestParams(safeWriteArgs(pjp == null ? null : pjp.getArgs()));

        if (result instanceof R<?> rr) {
            r.setResponseStatus(rr.getCode());
            r.setResponseResult(safeWrite(rr.getData()));
        } else if (result != null) {
            r.setResponseResult(safeWrite(result));
        }

        if (error != null) {
            r.setResponseStatus(r.getResponseStatus() == null ? 500 : r.getResponseStatus());
            r.setErrorStack(stack(error));
        }

        if (StringUtils.hasText(op.detailTemplateCode())) {
            r.setDetailTemplateCode(op.detailTemplateCode());
        }
        if (op.detailFields() != null && op.detailFields().length > 0) {
            r.setDetailFields(resolveDetailFields(op.detailFields(), result, pjp == null ? null : pjp.getArgs()));
        }

        recorder.record(r);
    }

    private Map<String, Object> resolveDetailFields(String[] fields, Object result, Object[] args) {
        Object root = unwrapResult(result);
        Map<String, Object> map = new LinkedHashMap<>();
        for (String f : fields) {
            if (!StringUtils.hasText(f)) continue;
            Object v = readPath(root, f);
            if (v == null && args != null) {
                for (Object a : args) {
                    v = readPath(a, f);
                    if (v != null) break;
                }
            }
            map.put(f, v);
        }
        return map;
    }

    private Object unwrapResult(Object result) {
        if (result instanceof R<?> rr) {
            return rr.getData();
        }
        return result;
    }

    private Object readPath(Object obj, String path) {
        if (obj == null || !StringUtils.hasText(path)) {
            return null;
        }
        if (obj instanceof Map<?, ?> m) {
            return m.get(path);
        }
        try {
            BeanWrapper bw = new BeanWrapperImpl(obj);
            if (bw.isReadableProperty(path)) {
                return bw.getPropertyValue(path);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String safeWriteArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object[] filtered = Arrays.stream(args)
                .filter(a -> a != null)
                .filter(a -> !(a instanceof HttpServletRequest))
                .filter(a -> !(a instanceof jakarta.servlet.http.HttpServletResponse))
                .filter(a -> !(a instanceof MultipartFile))
                .toArray();
        return safeWrite(filtered);
    }

    private String safeWrite(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            String s = objectMapper.writeValueAsString(obj);
            if (s.length() > 8000) {
                return s.substring(0, 8000);
            }
            return s;
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private String stack(Throwable t) {
        if (t == null) return null;
        String s = t.toString();
        if (s.length() > 2000) {
            return s.substring(0, 2000);
        }
        return s;
    }

    private HttpServletRequest currentRequest() {
        try {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                return attrs.getRequest();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Client-IP");
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
