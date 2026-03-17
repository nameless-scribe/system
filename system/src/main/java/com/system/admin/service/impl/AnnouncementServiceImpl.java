package com.system.admin.service.impl;

import com.system.admin.entity.Announcement;
import com.system.admin.mapper.AnnouncementMapper;
import com.system.admin.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> listAll() {
        return announcementMapper.selectAll();
    }

    @Override
    public List<Announcement> listPublished() {
        return announcementMapper.selectPublished();
    }

    @Override
    public void create(Announcement ann) {
        validateAnnouncement(ann);
        if (ann.getStatus() == null) {
            ann.setStatus(1);
        }
        announcementMapper.insert(ann);
    }

    @Override
    public void update(Announcement ann) {
        validateAnnouncement(ann);
        announcementMapper.update(ann);
    }

    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    /**
     * 公告基础校验：标题、内容必填，长度限制等
     */
    private void validateAnnouncement(Announcement ann) {
        if (ann == null) {
            throw new IllegalArgumentException("公告信息不能为空");
        }
        String title = ann.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("公告标题长度不能超过 100 个字符");
        }
        String content = ann.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("公告内容不能为空");
        }
        if (content.length() > 4000) {
            throw new IllegalArgumentException("公告内容长度不能超过 4000 个字符");
        }
    }
}

