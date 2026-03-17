package com.system.admin.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户举报
 */
@Data
public class Report {

    private Long id;
    private Long reporterId;
    private String targetType; // GOODS / ORDER / USER
    private Long targetId;
    /**
     * 目标对象的名称（如：被举报用户的用户名、商品名等），用于后台展示
     */
    private String targetName;
    private String reason;
    private String status; // PENDING / RESOLVED
    private String result;
    private Date createTime;
    private Date handleTime;
    private Long handlerId;
}

