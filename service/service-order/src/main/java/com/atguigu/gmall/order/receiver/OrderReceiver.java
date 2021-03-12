package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class OrderReceiver {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) {


        if (orderId != null) {
            OrderInfo orderInfo = orderService.getById(orderId);
            if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus())
                    && "UNPAID".equals(orderInfo.getProcessStatus())) {
                //校验订单状态是否是未支付
                PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());

                if (paymentInfo != null && "UNPAID".equals(paymentInfo.getPaymentStatus())) {
                    //校验有没有支付记录
                    Boolean flag = paymentFeignClient.checkPayment(orderId);
                    if (flag) {
                        //校验有没有支付
                        Boolean result = paymentFeignClient.closePay(orderId);
                        if (result) {
                            //校验支付宝订单有没有关闭
                            orderService.execExpiredOrder(orderId, "2");
                        } else {

                        }
                    } else {
                        //没有支付，支付宝不存在记录
                        orderService.execExpiredOrder(orderId, "2");
                    }
                } else {
                    //没有支付记录则只关闭订单
                    orderService.execExpiredOrder(orderId, "1");
                }
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void updOrder(Long orderId, Message message, Channel channel) {

        try {
            if (orderId != null) {
                OrderInfo orderInfo = orderService.getById(orderId);

                if (orderInfo != null && orderInfo.getOrderStatus().equals(ProcessStatus.UNPAID.name())) {
                    orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                    orderService.sendOrderStatus(orderId);
                }
            }
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            e.printStackTrace();
            return;
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_WARE_ORDER)
    public void updateOrder(String jsonStr, Message message, Channel channel) {

        if (!StringUtils.isEmpty(jsonStr)) {
            Map map = JSON.parseObject(jsonStr, Map.class);
            String status = (String) map.get("status");
            String orderId = (String) map.get("orderId");

            if ("DEDUCTED".equals(status)) {
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);

            } else {
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
