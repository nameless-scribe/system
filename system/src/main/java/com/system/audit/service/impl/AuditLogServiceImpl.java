package com.system.audit.service.impl;

import com.system.audit.entity.AuditLog;
import com.system.audit.mapper.AuditLogMapper;
import com.system.audit.service.AuditLogService;
import com.system.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Async
    @Override
    public void saveAsync(AuditLog log) {
        if (log == null) {
            return;
        }
        if (log.getId() == null) {
            log.setId(SnowflakeIdGenerator.generateId());
        }
        auditLogMapper.insert(log);
    }

    @Override
    public AuditLog getById(Long id) {
        if (id == null) {
            return null;
        }
        return auditLogMapper.selectById(id);
    }

    @Override
    public Map<String, Object> page(String operatorName,
                                    String uri,
                                    Integer success,
                                    Date startTime,
                                    Date endTime,
                                    int page,
                                    int size) {
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }
        int start = (page - 1) * size;

        List<AuditLog> list = auditLogMapper.selectPage(operatorName, uri, success, startTime, endTime, start, size);
        int total = auditLogMapper.count(operatorName, uri, success, startTime, endTime);
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", list);
        resp.put("total", total);
        resp.put("page", page);
        resp.put("size", size);
        return resp;
    }
}

