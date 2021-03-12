package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;

    @GetMapping("/list/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit) {

        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> pageModel = manageService.getPage(skuInfoPage);

        return Result.ok(pageModel);
    }

    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {

        manageService.onSale(skuId);
        return Result.ok();
    }

    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {

        manageService.cancelSale(skuId);
        return Result.ok();
    }

    @PostMapping("inner/findBaseCategory3ByCategory3IdList")
    public List<BaseCategory3> findBaseCategory3ByCategory3IdList(@RequestBody List<Long> category3IdList) {
        return manageService.findBaseCategory3ByCategory3IdList(category3IdList);
    }

}
