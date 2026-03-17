package com.system.admin.entity;

import lombok.Data;

/**
 * 字典数据实体，例如：goods_status 中的 0-未上架、1-交易中 等
 */
@Data
public class DictData {

    private Long id;
    /** 字典类型编码，对应 DictType.code */
    private String typeCode;
    /** 实际存储的值，例如：0、1、2 */
    private String value;
    /** 展示的标签，例如：待审核、交易中 */
    private String label;
    /** 排序 */
    private Integer sort;
    /** 状态：1-启用 0-停用 */
    private Integer status;
    /** 颜色（可选，用于前端 tag 的类型或颜色） */
    private String color;
    /** 备注 */
    private String remark;
}

