package com.system.admin.entity;

import lombok.Data;

/**
 * 商品/闲置物品实体
 */
@Data
public class Goods {

    private Long id;
    private String name;
    private Double price;
    private Long brandId;
    private Integer stock;
    private Integer status; // 0 下架 1 上架
    /** 审核状态：0待审核 1已通过 2被退回 */
    private Integer auditStatus;
    /** 审核/退回原因 */
    private String auditRemark;
    private String imageUrl;
    private String description;
    /** 发布人用户ID，用于“我发布的”列表和交易统计 */
    private Long ownerId;
    /** 新旧程度：1全新 2九成新 3八成新 4七成及以下 */
    private Integer conditionLevel;
    /** 浏览量/热度 */
    private Integer viewCount;
    /** 逻辑删除：0-未删除 1-已删除 */
    private Integer deleteStatus;
}

