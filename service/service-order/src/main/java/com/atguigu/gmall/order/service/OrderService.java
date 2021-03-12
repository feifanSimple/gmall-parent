package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;


public interface OrderService extends IService<OrderInfo> {

    Long saveOrderInfo(OrderInfo orderInfo);

    String getTradeNo(String userId);

    Boolean checkTradeCode(String userId, String tradeCode);

    void deleteTradeNo(String userId);

    Boolean checkStock(Long skuId, Integer skuNum);

    void execExpiredOrder(Long orderId);

    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    OrderInfo getOrderInfo(Long OrderId);

    void sendOrderStatus(Long orderId);

    Map initWareOrder(OrderInfo orderInfo);

    List<OrderInfo> orderSplit(Long orderId, String wareMap);

    void execExpiredOrder(Long orderId,String flag);
}
