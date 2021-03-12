package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        HashMap<String, Object> map = new HashMap<>();
        //获取用的收获地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        //获取用户选择的商品
        List<CartInfo> cartInfoList  = cartFeignClient.getCartCheckedList(userId);
        //声明一个储存订单明细的集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        //防止用户回退重复，获取流水号
        String tradeNo = orderService.getTradeNo(userId);
        map.put("tradeNo", tradeNo);
        map.put("userAddressList", userAddressList);
        map.put("detailArrayList", orderDetailList);
        map.put("totalNum", orderDetailList.size());
        map.put("totalAmount", orderInfo.getTotalAmount());



        return Result.ok(map);
    }

    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        //v防止用户回退
        String tradeNo = request.getParameter("tradeNo");
        Boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            return Result.fail().message("不能重复提交订单!");
        }
        //开启异步编排
        List<CompletableFuture> futureList  = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //校验库存是否充足
            CompletableFuture<Void> checkStockCompletableFuture = CompletableFuture.runAsync(() -> {
                Boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!result) {
                    errorList.add(orderDetail.getSkuName() + "库存不足！");
                }
            }, threadPoolExecutor);
            futureList.add(checkStockCompletableFuture);

            //查看价格是否是最新的
            CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
                //  验证价格： 当前订单价格与实际商品的价格是否一直  skuInfo.price();
                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                //  比较价格
                if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                    //  更新一下商品的价格
                    cartFeignClient.loadCartCache(userId);
                    //  提示
                    //  return  Result.fail().message(orderDetail.getSkuName()+"\t 价格有变动!");
                    errorList.add(orderDetail.getSkuName() + "\t 价格有变动!");
                }
            }, threadPoolExecutor);
            futureList.add(skuPriceCompletableFuture);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        if (errorList.size() != 0) {
            return Result.fail().message(StringUtils.join(errorList, ","));
        }
        orderService.deleteTradeNo(userId);
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId) {
        return orderService.getOrderInfo(orderId);
    }

    @RequestMapping("orderSplit")
    public String orderSplit(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        List<OrderInfo> orderInfoList = orderService.orderSplit(Long.parseLong(orderId), wareSkuMap);
        ArrayList<Map> maps = new ArrayList<>();

        for (OrderInfo orderInfo : orderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            maps.add(map);
        }

        return JSON.toJSONString(maps);
    }

    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo) {
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return orderId;
    }
}
