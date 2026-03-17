package com.system.audit.service;

import com.system.audit.entity.AuditLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AuditLogService {
    void saveAsync(AuditLog log);

    AuditLog getById(Long id);

    Map<String, Object> page(String operatorName,
                             String uri,
                             Integer success,
                             Date startTime,
                             Date endTime,
                             int page,
                             int size);
}

