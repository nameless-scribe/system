package com.system.rbac.entity;

import lombok.Data;

@Data
public class Role {
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private Integer sort;
    private String remark;
}

