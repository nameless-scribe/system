package com.system.user.entity;

import lombok.Data;

/**
 * 用户实体（简化版）
 */
@Data
public class User {

    private Long id;
    private String username;
    private String password;
    /**
     * 角色：USER / ADMIN / SUPER_ADMIN
     */
    private String role; // USER / ADMIN / SUPER_ADMIN
    /**
     * 状态：1-正常 0-封禁
     */
    private Integer status;

    /**
     * 注册/创建时间（用于店铺“来到平台累计时间”展示）
     */
    private java.util.Date createTime;
}

