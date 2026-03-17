package com.system.admin.entity;

import lombok.Data;

/**
 * 字典类型实体，例如：goods_status、brand_status、user_status 等
 */
@Data
public class DictType {

    private Long id;
    /** 字典编码，唯一，如：goods_status */
    private String code;
    /** 字典名称，如：商品状态 */
    private String name;
    /** 状态：1-启用 0-停用 */
    private Integer status;
    /** 备注 */
    private String remark;
}

