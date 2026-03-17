package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 简单交易评价实体
 */
@Data
public class Rating {

    private Long id;
    private Long orderId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score; // 1-5
    private String comment;
    private Date createdTime;
}

