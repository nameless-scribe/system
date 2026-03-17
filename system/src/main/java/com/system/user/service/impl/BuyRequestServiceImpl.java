package com.system.user.service.impl;

import com.system.user.entity.BuyRequest;
import com.system.user.entity.BuyRequestComment;
import com.system.user.entity.BuyRequestCommentView;
import com.system.user.entity.BuyRequestView;
import com.system.user.mapper.BuyRequestCommentMapper;
import com.system.user.mapper.BuyRequestLikeMapper;
import com.system.user.mapper.BuyRequestMapper;
import com.system.user.service.BuyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BuyRequestServiceImpl implements BuyRequestService {

    @Autowired
    private BuyRequestMapper buyRequestMapper;

    @Autowired
    private BuyRequestCommentMapper commentMapper;

    @Autowired
    private BuyRequestLikeMapper likeMapper;

    @Override
    public Map<String, Object> listSquare(int page, int size) {
        int p = page <= 0 ? 1 : page;
        int s = size <= 0 ? 10 : Math.min(size, 50);

        List<BuyRequestView> pinned = buyRequestMapper.selectPinnedTop(5);
        List<Long> exclude = pinned == null ? List.of() : pinned.stream().map(BuyRequestView::getId).collect(Collectors.toList());

        int start = (p - 1) * s;
        List<BuyRequestView> list = buyRequestMapper.selectLatestExcludeIds(exclude, start, s);
        int total = buyRequestMapper.countLatestExcludeIds(exclude);

        Map<String, Object> resp = new HashMap<>();
        resp.put("pinned", pinned);
        resp.put("list", list);
        resp.put("page", p);
        resp.put("size", s);
        resp.put("total", total);
        return resp;
    }

    @Override
    public BuyRequestView detail(Long id) {
        if (id == null) return null;
        return buyRequestMapper.selectViewById(id);
    }

    @Override
    public List<BuyRequestCommentView> comments(Long requestId) {
        if (requestId == null) return List.of();
        return commentMapper.selectByRequestId(requestId);
    }

    @Override
    public void publish(Long userId, String title, String content, BigDecimal priceMin, BigDecimal priceMax, String contact) {
        if (userId == null) throw new IllegalArgumentException("未登录");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("标题不能为空");
        if (title.length() > 120) throw new IllegalArgumentException("标题长度不能超过 120");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("内容不能为空");
        if (content.length() > 2000) throw new IllegalArgumentException("内容长度不能超过 2000");
        if (contact != null && contact.length() > 200) throw new IllegalArgumentException("联系方式长度不能超过 200");
        if (priceMin != null && priceMin.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("最低价不能为负");
        if (priceMax != null && priceMax.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("最高价不能为负");
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) throw new IllegalArgumentException("最低价不能大于最高价");

        BuyRequest r = new BuyRequest();
        r.setUserId(userId);
        r.setTitle(title.trim());
        r.setContent(content.trim());
        r.setPriceMin(priceMin);
        r.setPriceMax(priceMax);
        r.setContact(contact);
        r.setStatus(1);
        buyRequestMapper.insert(r);
    }

    @Override
    public void addComment(Long userId, Long requestId, String content) {
        if (userId == null) throw new IllegalArgumentException("未登录");
        if (requestId == null) throw new IllegalArgumentException("requestId 不能为空");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("评论不能为空");
        if (content.length() > 800) throw new IllegalArgumentException("评论长度不能超过 800");

        BuyRequest req = buyRequestMapper.selectById(requestId);
        if (req == null || req.getStatus() == null || req.getStatus() != 1) {
            throw new IllegalArgumentException("求购信息不存在或已下架");
        }

        BuyRequestComment c = new BuyRequestComment();
        c.setRequestId(requestId);
        c.setUserId(userId);
        c.setContent(content.trim());
        c.setStatus(1);
        commentMapper.insert(c);
        buyRequestMapper.incCommentCount(requestId, 1);
    }

    @Override
    public boolean like(Long userId, Long requestId) {
        if (userId == null) throw new IllegalArgumentException("未登录");
        if (requestId == null) throw new IllegalArgumentException("requestId 不能为空");
        try {
            likeMapper.insert(requestId, userId);
            buyRequestMapper.incLikeCount(requestId, 1);
            return true;
        } catch (DuplicateKeyException ignore) {
            return false;
        }
    }

    @Override
    public boolean unlike(Long userId, Long requestId) {
        if (userId == null) throw new IllegalArgumentException("未登录");
        if (requestId == null) throw new IllegalArgumentException("requestId 不能为空");
        int deleted = likeMapper.delete(requestId, userId);
        if (deleted > 0) {
            buyRequestMapper.incLikeCount(requestId, -1);
            return true;
        }
        return false;
    }

    @Override
    public boolean liked(Long userId, Long requestId) {
        if (userId == null || requestId == null) return false;
        return likeMapper.countByReqAndUser(requestId, userId) > 0;
    }

    @Override
    public Map<String, Object> adminList(String keyword, Integer status, int page, int size) {
        int p = page <= 0 ? 1 : page;
        int s = size <= 0 ? 10 : Math.min(size, 50);
        int start = (p - 1) * s;
        List<BuyRequestView> list = buyRequestMapper.selectAdminList(keyword, status, start, s);
        int total = buyRequestMapper.countAdminList(keyword, status);
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", list);
        resp.put("total", total);
        resp.put("page", p);
        resp.put("size", s);
        return resp;
    }

    @Override
    public void adminDelete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        BuyRequest req = buyRequestMapper.selectById(id);
        if (req == null) {
            throw new IllegalArgumentException("求购信息不存在");
        }
        buyRequestMapper.updateStatus(id, 0);
    }
}

