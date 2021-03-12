package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface CouponInfoService extends IService<CouponInfo> {

    IPage<CouponInfo> selectPage(Page<CouponInfo> pageParam);

    void saveCouponRule(CouponRuleVo couponRuleVo);

    Map<String, Object> findCouponRuleList(Long id);

    List<CouponInfo> findCouponByKeyword(String keyword);
}
