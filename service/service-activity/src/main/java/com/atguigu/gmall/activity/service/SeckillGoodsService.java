package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    List<SeckillGoods> findAll();

    SeckillGoods getSeckillGoods(Long id);

    void seckillOrder(Long skuId, String userId);

    Result checkOrder(Long skuId, String userId);
}
