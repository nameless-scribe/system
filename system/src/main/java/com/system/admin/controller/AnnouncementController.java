package com.system.admin.controller;

import com.system.admin.entity.Announcement;
import com.system.admin.service.AnnouncementService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 + 前台公告接口
 */
@RestController
@RequestMapping("/api")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 管理端：公告列表
     */
    @GetMapping("/admin/announcements")
    public Result<List<Announcement>> adminList() {
        return Result.success(announcementService.listAll());
    }

    /**
     * 管理端：新增公告
     */
    @PostMapping("/admin/announcements")
    public Result<Void> create(@RequestBody Announcement ann) {
        announcementService.create(ann);
        return Result.success();
    }

    /**
     * 管理端：编辑公告
     */
    @PutMapping("/admin/announcements/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Announcement ann) {
        ann.setId(id);
        announcementService.update(ann);
        return Result.success();
    }

    /**
     * 管理端：删除公告
     */
    @DeleteMapping("/admin/announcements/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }

    /**
     * 前台：获取已发布公告
     */
    @GetMapping("/announcements")
    public Result<List<Announcement>> listPublished() {
        return Result.success(announcementService.listPublished());
    }
}

