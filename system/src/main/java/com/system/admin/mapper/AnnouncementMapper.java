package com.system.admin.mapper;

import com.system.admin.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    List<Announcement> selectAll();

    List<Announcement> selectPublished();

    Announcement selectById(@Param("id") Long id);

    int insert(Announcement ann);

    int update(Announcement ann);

    int deleteById(@Param("id") Long id);
}

