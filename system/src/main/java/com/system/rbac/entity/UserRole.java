package com.system.rbac.entity;

import lombok.Data;

@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
}

