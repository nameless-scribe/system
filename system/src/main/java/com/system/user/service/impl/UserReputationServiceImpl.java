package com.system.user.service.impl;

import com.system.user.entity.Rating;
import com.system.user.entity.UserReputation;
import com.system.user.mapper.UserReputationMapper;
import com.system.user.service.UserReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class UserReputationServiceImpl implements UserReputationService {

    @Autowired
    private UserReputationMapper reputationMapper;

    @Override
    public void updateOnNewRating(Rating rating) {
        if (rating == null || rating.getToUserId() == null || rating.getScore() == null) {
            return;
        }
        Long toUserId = rating.getToUserId();
        Integer score = rating.getScore();

        UserReputation rep = reputationMapper.selectByUserId(toUserId);
        if (rep == null) {
            rep = new UserReputation();
            rep.setUserId(toUserId);
            rep.setTotalScore(0);
            rep.setTotalCount(0);
            rep.setGoodCount(0);
        }

        rep.setTotalScore(rep.getTotalScore() + score);
        rep.setTotalCount(rep.getTotalCount() + 1);
        if (score >= 4) {
            rep.setGoodCount(rep.getGoodCount() + 1);
        }

        // 好评率
        double positiveRate = rep.getTotalCount() == 0 ? 0.0 :
                (rep.getGoodCount() * 100.0 / rep.getTotalCount());
        rep.setPositiveRate(BigDecimal.valueOf(positiveRate).setScale(2, RoundingMode.HALF_UP));

        // 平均星级（1~5）
        double avgStar = rep.getTotalCount() == 0 ? 0.0 :
                (rep.getTotalScore() * 1.0 / rep.getTotalCount());
        int baseScore = (int) Math.round(avgStar / 5.0 * 80); // 0~80
        int countBonus = (int) Math.round(Math.min(rep.getTotalCount(), 50) / 50.0 * 20); // 0~20
        int finalScore = Math.max(0, Math.min(100, baseScore + countBonus));
        rep.setScore(finalScore);

        // 等级
        String level;
        if (rep.getTotalCount() < 3) {
            level = "NEW";
        } else if (finalScore >= 90) {
            level = "GOOD";
        } else if (finalScore >= 60) {
            level = "NORMAL";
        } else {
            level = "RISK";
        }
        rep.setLevel(level);
        rep.setLastCalcTime(new Date());

        if (reputationMapper.selectByUserId(toUserId) == null) {
            reputationMapper.insert(rep);
        } else {
            reputationMapper.update(rep);
        }
    }

    @Override
    public UserReputation getByUserId(Long userId) {
        return reputationMapper.selectByUserId(userId);
    }
}

