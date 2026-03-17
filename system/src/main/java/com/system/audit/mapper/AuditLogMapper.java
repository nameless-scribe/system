package com.system.audit.mapper;

import com.system.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog log);

    AuditLog selectById(@Param("id") Long id);

    List<AuditLog> selectPage(@Param("operatorName") String operatorName,
                              @Param("uri") String uri,
                              @Param("success") Integer success,
                              @Param("startTime") Date startTime,
                              @Param("endTime") Date endTime,
                              @Param("start") int start,
                              @Param("size") int size);

    int count(@Param("operatorName") String operatorName,
              @Param("uri") String uri,
              @Param("success") Integer success,
              @Param("startTime") Date startTime,
              @Param("endTime") Date endTime);
}

