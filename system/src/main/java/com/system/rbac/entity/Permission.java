package com.system.rbac.entity;

import lombok.Data;

@Data
public class Permission {
    private Long id;
    private String code;
    private String name;
    private String module;
    private String type;
    private Integer status;
    private Integer sort;
    private String remark;
}

