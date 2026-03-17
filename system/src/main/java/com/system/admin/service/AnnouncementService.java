package com.system.admin.service;

import com.system.admin.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    List<Announcement> listAll();

    List<Announcement> listPublished();

    void create(Announcement ann);

    void update(Announcement ann);

    void delete(Long id);
}

