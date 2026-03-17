package com.system.admin.service.impl;

import com.system.admin.entity.DictData;
import com.system.admin.entity.DictType;
import com.system.admin.mapper.DictDataMapper;
import com.system.admin.mapper.DictTypeMapper;
import com.system.admin.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private DictTypeMapper dictTypeMapper;

    @Autowired
    private DictDataMapper dictDataMapper;

    @Override
    public List<DictType> listTypes() {
        return dictTypeMapper.selectAll();
    }

    @Override
    public void addType(DictType dictType) {
        validateDictType(dictType);
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        dictTypeMapper.insert(dictType);
    }

    @Override
    public void updateType(DictType dictType) {
        validateDictType(dictType);
        dictTypeMapper.update(dictType);
    }

    @Override
    public void deleteType(Long id) {
        dictTypeMapper.deleteById(id);
    }

    @Override
    public List<DictData> listAllData() {
        return dictDataMapper.selectAll();
    }

    @Override
    public List<DictData> listDataByType(String typeCode) {
        return dictDataMapper.selectByTypeCode(typeCode);
    }

    @Override
    public void addData(DictData dictData) {
        validateDictData(dictData);
        if (dictData.getStatus() == null) {
            dictData.setStatus(1);
        }
        if (dictData.getSort() == null) {
            dictData.setSort(0);
        }
        dictDataMapper.insert(dictData);
    }

    @Override
    public void updateData(DictData dictData) {
        validateDictData(dictData);
        dictDataMapper.update(dictData);
    }

    @Override
    public void deleteData(Long id) {
        dictDataMapper.deleteById(id);
    }

    /**
     * 字典类型基础校验：编码、名称必填
     */
    private void validateDictType(DictType dictType) {
        if (dictType == null) {
            throw new IllegalArgumentException("字典类型信息不能为空");
        }
        String code = dictType.getCode();
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("字典类型编码不能为空");
        }
        if (code.length() > 64) {
            throw new IllegalArgumentException("字典类型编码长度不能超过 64 个字符");
        }
        String name = dictType.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("字典类型名称不能为空");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("字典类型名称长度不能超过 100 个字符");
        }
    }

    /**
     * 字典数据基础校验：所属类型、标签、值必填
     */
    private void validateDictData(DictData data) {
        if (data == null) {
            throw new IllegalArgumentException("字典数据信息不能为空");
        }
        String typeCode = data.getTypeCode();
        if (typeCode == null || typeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("字典类型编码不能为空");
        }
        if (typeCode.length() > 64) {
            throw new IllegalArgumentException("字典类型编码长度不能超过 64 个字符");
        }
        String label = data.getLabel();
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("字典标签不能为空");
        }
        if (label.length() > 100) {
            throw new IllegalArgumentException("字典标签长度不能超过 100 个字符");
        }
        String value = data.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("字典值不能为空");
        }
        if (value.length() > 64) {
            throw new IllegalArgumentException("字典值长度不能超过 64 个字符");
        }
    }
}

