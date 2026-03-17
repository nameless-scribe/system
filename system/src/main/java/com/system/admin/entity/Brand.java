package com.system.admin.entity;

import lombok.Data;

/**
 * 品牌实体（简化版）
 */
@Data
public class Brand {

    private Long id;
    private String name;
    private String logoUrl;
    private String description;
}

