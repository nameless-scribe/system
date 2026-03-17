package com.system.user.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户信誉信息
 */
@Data
public class UserReputation {

    private Long userId;
    /** 累计评分总和（1~5 累加） */
    private Integer totalScore;
    /** 累计评价次数 */
    private Integer totalCount;
    /** 好评次数（score >= 4） */
    private Integer goodCount;
    /** 当前信誉分：0~100 */
    private Integer score;
    /** 等级：NEW / NORMAL / GOOD / RISK */
    private String level;
    /** 好评率：0~100（百分比，保留两位小数） */
    private BigDecimal positiveRate;
    /** 最近一次计算时间 */
    private Date lastCalcTime;
}

