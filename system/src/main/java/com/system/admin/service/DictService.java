package com.system.admin.service;

import com.system.admin.entity.DictData;
import com.system.admin.entity.DictType;

import java.util.List;

public interface DictService {

    // 类型管理
    List<DictType> listTypes();

    void addType(DictType dictType);

    void updateType(DictType dictType);

    void deleteType(Long id);

    // 数据管理
    List<DictData> listAllData();

    List<DictData> listDataByType(String typeCode);

    void addData(DictData dictData);

    void updateData(DictData dictData);

    void deleteData(Long id);
}

