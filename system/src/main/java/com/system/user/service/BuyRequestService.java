package com.system.user.service;

import com.system.user.entity.BuyRequestView;
import com.system.user.entity.BuyRequestCommentView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BuyRequestService {
    Map<String, Object> listSquare(int page, int size);

    BuyRequestView detail(Long id);

    List<BuyRequestCommentView> comments(Long requestId);

    void publish(Long userId, String title, String content, BigDecimal priceMin, BigDecimal priceMax, String contact);

    void addComment(Long userId, Long requestId, String content);

    boolean like(Long userId, Long requestId);

    boolean unlike(Long userId, Long requestId);

    boolean liked(Long userId, Long requestId);

    Map<String, Object> adminList(String keyword, Integer status, int page, int size);

    void adminDelete(Long id);
}

