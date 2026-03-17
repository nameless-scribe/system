package com.system.user.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 求购信息
 */
@Data
public class BuyRequest {
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
    private Date updateTime;
}

