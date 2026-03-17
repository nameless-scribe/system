package com.system.admin.controller;

import com.system.admin.entity.DictData;
import com.system.admin.entity.DictType;
import com.system.admin.service.DictService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：字典管理（类型 + 数据）
 */
@RestController
@RequestMapping("/api/admin/dict")
public class AdminDictController {

    @Autowired
    private DictService dictService;

    // -------- 字典类型管理 --------

    @GetMapping("/types")
    public Result<List<DictType>> listTypes() {
        return Result.success(dictService.listTypes());
    }

    @PostMapping("/types")
    public Result<Void> addType(@RequestBody DictType dictType) {
        dictService.addType(dictType);
        return Result.success();
    }

    @PutMapping("/types/{id}")
    public Result<Void> updateType(@PathVariable Long id, @RequestBody DictType dictType) {
        dictType.setId(id);
        dictService.updateType(dictType);
        return Result.success();
    }

    @DeleteMapping("/types/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    // -------- 字典数据管理 --------

    @GetMapping("/data")
    public Result<List<DictData>> listAllData() {
        return Result.success(dictService.listAllData());
    }

    @GetMapping("/data/{typeCode}")
    public Result<List<DictData>> listDataByType(@PathVariable String typeCode) {
        return Result.success(dictService.listDataByType(typeCode));
    }

    @PostMapping("/data")
    public Result<Void> addData(@RequestBody DictData dictData) {
        dictService.addData(dictData);
        return Result.success();
    }

    @PutMapping("/data/{id}")
    public Result<Void> updateData(@PathVariable Long id, @RequestBody DictData dictData) {
        dictData.setId(id);
        dictService.updateData(dictData);
        return Result.success();
    }

    @DeleteMapping("/data/{id}")
    public Result<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return Result.success();
    }
}

