package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.service.BaseSaleAttrService;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/product")
@Api(tags = "商品销售属性接口")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    @Autowired
    private BaseSaleAttrService baseSaleAttrService;


    @GetMapping("{page}/{size}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long size,
                                 SpuInfo spuInfo) {

        Page<SpuInfo> spuInfoPage = new Page<>(page, size);
       IPage<SpuInfo> spuInfoPageList  =  manageService.getSpuInfoPage(spuInfoPage, spuInfo);

       return Result.ok(spuInfoPageList);
    }

    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList() {

        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {

        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    @GetMapping("findSpuInfoByKeyword/{keyword}")
    public Result findSpuInfoByKeyword(@PathVariable String keyword) {

        List<SpuInfo> spuInfoList = manageService.findSpuInfoByKeyword(keyword);

        return Result.ok(spuInfoList);
    }

    @PostMapping("inner/findSpuInfoBySpuIdList")
    public List<SpuInfo> findSpuInfoBySpuIdList(@RequestBody List<Long> spuIdList) {

        List<SpuInfo> spuInfoList = manageService.findSpuInfoBySpuIdList(spuIdList);
        return spuInfoList;
    }
}
