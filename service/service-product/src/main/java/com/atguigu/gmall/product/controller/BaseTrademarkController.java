package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/baseTrademark")
@Api(tags = "商品品牌属性接口")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit) {

        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        IPage<BaseTrademark> pageModel = baseTrademarkService.selectPage(baseTrademarkPage);
        return Result.ok(pageModel);
    }

    @PostMapping("save")
    @ApiOperation("新增商品商标")
    public Result save(@RequestBody BaseTrademark banner) {

        baseTrademarkService.save(banner);
        return Result.ok();
    }

    @ApiOperation("修改商品商标")
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark banner) {

        baseTrademarkService.updateById(banner);
        return Result.ok();
    }

    @DeleteMapping("remove/{id}")
    @ApiOperation("删除BaseTrademark")
    public Result remove(@PathVariable Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    @GetMapping("get/{id}")
    @ApiOperation("获取BaseTrademark")
    public Result get(@PathVariable String id) {
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    @GetMapping("getTrademarkList")
    public Result getTrademarkList() {

        List<BaseTrademark> baseTrademarkList = baseTrademarkService.list(null);
        return Result.ok(baseTrademarkList);
    }

    @GetMapping("findBaseTrademarkByKeyword/{keyword}")
    public Result findBaseTrademarkByKeyword(@PathVariable String keyword) {

       List<BaseTrademark> trademarkList = baseTrademarkService.findBaseTrademarkByKeyword(keyword);
        return Result.ok(trademarkList);
    }

    @PostMapping("inner/findBaseTrademarkByTrademarkIdList")
    public List<BaseTrademark> findBaseTrademarkByTrademarkIdList(@RequestBody List<Long> trademarkIdList) {

        List<BaseTrademark> trademarkList = baseTrademarkService.findBaseTrademarkByTrademarkIdList(trademarkIdList);

        return trademarkList;
    }
}
