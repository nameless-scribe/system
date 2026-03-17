package com.system.user.entity;

import lombok.Data;

import java.util.Date;

@Data
public class BuyRequestComment {
    private Long id;
    private Long requestId;
    private Long userId;
    private String content;
    private Integer status;
    private Date createTime;
}

