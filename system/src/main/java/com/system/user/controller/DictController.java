package com.system.user.controller;

import com.system.admin.entity.DictData;
import com.system.admin.service.DictService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台/用户端：根据类型编码获取启用的字典数据
 */
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/{typeCode}")
    public Result<List<DictData>> listByType(@PathVariable String typeCode) {
        return Result.success(dictService.listDataByType(typeCode));
    }
}

