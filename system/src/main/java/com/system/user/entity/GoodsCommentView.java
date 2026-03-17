package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 商品历史成交评论（用于商品详情页展示）
 */
@Data
public class GoodsCommentView {
    private Long orderId;
    private Integer score;
    private String comment;
    private Date createdTime;
    private Long fromUserId;
    private String fromUsername;
}

