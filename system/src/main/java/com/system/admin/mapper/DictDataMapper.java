package com.system.admin.mapper;

import com.system.admin.entity.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictDataMapper {

    List<DictData> selectByTypeCode(@Param("typeCode") String typeCode);

    List<DictData> selectAll();

    int insert(DictData dictData);

    int update(DictData dictData);

    int deleteById(Long id);
}

