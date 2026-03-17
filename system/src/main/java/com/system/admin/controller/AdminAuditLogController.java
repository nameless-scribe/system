package com.system.admin.controller;

import com.system.audit.entity.AuditLog;
import com.system.audit.service.AuditLogService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * 管理端：系统日志（管理员操作审计）
 */
@RestController
@RequestMapping("/api/admin/auditLogs")
public class AdminAuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public Result<Map<String, Object>> page(@RequestParam(required = false) String operatorName,
                                           @RequestParam(required = false) String uri,
                                           @RequestParam(required = false) Integer success,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(auditLogService.page(operatorName, uri, success, startTime, endTime, page, size));
    }

    @GetMapping("/{id}")
    public Result<AuditLog> detail(@PathVariable Long id) {
        AuditLog al = auditLogService.getById(id);
        if (al == null) {
            return Result.fail("日志不存在");
        }
        return Result.success(al);
    }
}

