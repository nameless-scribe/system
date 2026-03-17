package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 收藏记录视图（携带商品信息，用于前端展示）
 */
@Data
public class FavoriteView {
    private Long id;
    private Long goodsId;
    private Date createTime;

    private String goodsName;
    private Double price;
    private String imageUrl;
    private Integer stock;
    private Long ownerId;

    // 商品状态信息（用于无效收藏标记）
    private Integer goodsStatus;      // 0下架 1上架
    private Integer auditStatus;      // 0待审核 1通过 2退回
    private Integer deleteStatus;     // 0未删除 1已删除
    private Long brandId;
    private String brandName;

    // 卖家信息/信誉
    private String sellerUsername;
    private Integer sellerRepScore;
    private String sellerRepLevel;
    private java.math.BigDecimal sellerPositiveRate;
}

