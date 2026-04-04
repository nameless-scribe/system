package com.system.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.audit.entity.AuditLog;
import com.system.audit.service.AuditLogService;
import com.system.common.Result;
import com.system.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 管理端操作审计日志：
 * - 自动拦截 com.system.admin.controller..* 的所有方法
 * - 记录操作人、URI、参数摘要、耗时与结果
 */
@Aspect
@Component
public class AdminAuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditLogAspect.class);

    private static final int MAX_BODY_LEN = 3000;
    private static final int MAX_ERR_LEN = 500;
    private static final Pattern PASSWORD_JSON = Pattern.compile("(\"password\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("execution(* com.system.admin.controller..*(..))")
    public Object aroundAdminController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        HttpServletRequest request = currentRequest();
        // 极端情况下拿不到 request，直接执行不影响业务
        if (request == null) {
            return pjp.proceed();
        }

        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/admin")) {
            return pjp.proceed();
        }

        User currentUser = (User) request.getAttribute("currentUser");
        AuditLog al = new AuditLog();
        if (currentUser != null) {
            al.setOperatorId(currentUser.getId());
            al.setOperatorName(currentUser.getUsername());
            al.setOperatorRole(currentUser.getRole());
        }
        al.setIp(clientIp(request));
        al.setHttpMethod(request.getMethod());
        al.setUri(uri);
        al.setQueryString(safeStr(request.getQueryString(), 1024));
        al.setRequestBody(truncateArgs(pjp.getArgs()));



        
        try {
            Object ret = pjp.proceed();
            al.setSuccess(1);
            // 尽量兼容统一返回体 Result
            if (ret instanceof Result) {
                al.setResultCode(((Result<?>) ret).getCode());
            } else {
                al.setResultCode(200);
            }
            return ret;
        } catch (Throwable ex) {
            al.setSuccess(0);
            al.setResultCode(500);
            al.setErrorMessage(safeStr(ex.getMessage(), MAX_ERR_LEN));
            throw ex;
        } finally {
            al.setCostMs(System.currentTimeMillis() - start);
            try {
                auditLogService.saveAsync(al);
            } catch (Exception e) {
                log.warn("写入审计日志失败：{}", e.getMessage());
            }
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private String truncateArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            // 简单过滤掉 request/response 这类无法序列化对象
            Object[] safeArgs = Arrays.stream(args)
                    .filter(a -> !(a instanceof HttpServletRequest))
                    .filter(a -> !(a instanceof MultipartFile))
                    .toArray();
            String json = objectMapper.writeValueAsString(safeArgs);
            // 避免落库敏感信息
            String masked = PASSWORD_JSON.matcher(json).replaceAll("$1***$3");
            return safeStr(masked, MAX_BODY_LEN);
        } catch (Exception e) {
            return safeStr(String.valueOf(Arrays.toString(args)), MAX_BODY_LEN);
        }
    }

    private String safeStr(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen);
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

