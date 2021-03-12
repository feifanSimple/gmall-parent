package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;


public interface AlipayService {


    String createAliPay(Long orderId) throws AlipayApiException;

    boolean refund(Long orderId);

    Boolean closePay(Long orderId);

    Boolean checkPayment(Long orderId);
}
