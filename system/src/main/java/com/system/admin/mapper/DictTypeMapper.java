package com.system.admin.mapper;

import com.system.admin.entity.DictType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictTypeMapper {

    List<DictType> selectAll();

    DictType selectByCode(String code);

    int insert(DictType dictType);

    int update(DictType dictType);

    int deleteById(Long id);
}

