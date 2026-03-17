package com.system.user.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 求购列表/详情视图（带发布人信息）
 */
@Data
public class BuyRequestView {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String contact;
    private Integer status;
    private Integer likeCount;
    private Integer commentCount;
    private Date createTime;

    private String username;
    private String avatar;
}

