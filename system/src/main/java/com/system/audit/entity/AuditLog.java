package com.system.audit.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AuditLog {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String ip;
    private String httpMethod;
    private String uri;
    private String queryString;
    private String requestBody;
    private Integer success;
    private Integer resultCode;
    private String errorMessage;
    private Long costMs;
    private Date createTime;
}

