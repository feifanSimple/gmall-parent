package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import sun.rmi.runtime.Log;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManageService {

    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getAttrInfo(Long attrId);

    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList(Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    IPage<SkuInfo> getPage(Page<SkuInfo> skuInfoPage);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuInfo getSkuInfo(Long skuId);

    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    BigDecimal getSkuPrice(Long skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    Map getSkuValueIdsMap(Long spuId);

    List<JSONObject> getBaseCategoryList();

    BaseTrademark getTrademarkByTmId(Long tmId);

    List<BaseAttrInfo> getAttrList(Long skuId);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    List<SkuInfo> findSkuInfoBySkuIdList(List<Long> skuIdlist);

    List<SpuInfo> findSpuInfoByKeyword(String keyword);

    List<SpuInfo> findSpuInfoBySpuIdList(List<Long> spuIdList);

    List<BaseCategory3> findBaseCategory3ByCategory3IdList(List<Long> category3IdList);
}
