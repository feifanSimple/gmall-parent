package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.CouponInfoMapper;
import com.atguigu.gmall.activity.mapper.CouponRangeMapper;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRange;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.atguigu.gmall.model.enums.CouponRangeType;
import com.atguigu.gmall.model.enums.CouponType;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ldap.PagedResultsControl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Autowired
    private ProductFeignClient productFeignClient;


    @Override
    public IPage<CouponInfo> selectPage(Page<CouponInfo> pageParam) {

        QueryWrapper<CouponInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        IPage<CouponInfo> couponInfoIPage = couponInfoMapper.selectPage(pageParam, queryWrapper);
        couponInfoIPage.getRecords().stream().forEach(couponInfo -> {
            couponInfo.setCouponTypeString(CouponType.getNameByType(couponInfo.getCouponType()));

            if (couponInfo.getRangeType() != null) {
                couponInfo.setRangeTypeString(CouponRangeType.getNameByType(couponInfo.getRangeType()));
            }

        });


        return couponInfoIPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        //删除范围表
        QueryWrapper<CouponRange> rangeQueryWrapper = new QueryWrapper<>();
        rangeQueryWrapper.eq("coupon_id", couponRuleVo.getCouponId());
        couponRangeMapper.delete(rangeQueryWrapper);

        CouponInfo couponInfo = this.getById(couponRuleVo.getCouponId());
        couponInfo.setRangeType(couponRuleVo.getRangeType().name());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setConditionNum(couponRuleVo.getConditionNum());
        couponInfo.setBenefitAmount(couponRuleVo.getBenefitAmount());
        couponInfo.setBenefitDiscount(couponRuleVo.getBenefitDiscount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        this.updateById(couponInfo);

        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            couponRange.setCouponId(couponRuleVo.getCouponId());
            couponRangeMapper.insert(couponRange);
        }

    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {

        HashMap<String, Object> map = new HashMap<>();
        CouponInfo couponInfo = this.getById(id);

        QueryWrapper<CouponRange> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", id);
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(queryWrapper);
        List<Long> spuIdList = couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        if (spuIdList != null) {
            if ("SPU".equals(couponInfo.getRangeType())) {
                List<SpuInfo> spuInfoList = productFeignClient.findSpuInfoBySpuIdList(spuIdList);
                map.put("spuInfoList", spuInfoList);
            }else if ("TRADEMARK".equals(couponInfo.getRangeType())) {
                List<BaseTrademark> trademarkList = productFeignClient.findBaseTrademarkByTrademarkIdList(spuIdList);
                map.put("trademarkList", trademarkList);
            }else {
                List<BaseCategory3> category3IdList = productFeignClient.findBaseCategory3ByCategory3IdList(spuIdList);
                map.put("category3List", category3IdList);
            }
        }
        return map;
    }

    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {

        QueryWrapper<CouponInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("coupon_name", keyword);

        List<CouponInfo> couponInfoList = couponInfoMapper.selectList(queryWrapper);
        return couponInfoList;
    }
}
