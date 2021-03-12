package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import java.util.Map;

@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService  paymentService;


    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String submitOrder(@PathVariable Long orderId,
                              HttpServletRequest request) {

        String form = "";
        try {
            form = alipayService.createAliPay(orderId);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

    @RequestMapping("callback/return")
    public String callBack() {

        return "redirect:"+ AlipayConfig.return_order_url;
    }

    @RequestMapping("callback/notify")
    @ResponseBody
    public String alipayNotify(@RequestParam Map<String, String> paramMap) {

        System.err.println("支付完成异步通知 :" + paramMap);
        //延签
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramMap,
                    AlipayConfig.alipay_public_key,
                    AlipayConfig.charset,
                    AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        String tradeStatus = paramMap.get("trade_status");
        String outTradeNo = paramMap.get("out_trade_no");

        if (signVerified) {
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {

             PaymentInfo paymentInfo =  paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());

             if (paymentInfo.getPaymentStatus().equals(PaymentStatus.PAID.name())
                     || paymentInfo.getPaymentStatus().equals(PaymentStatus.ClOSED)) {
                 return "failure";
             }

             paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY.name(), paramMap);
                return "success";
            }else {
                return "failure";
            }
        }
        return "failure";
    }

    @RequestMapping("refund/{orderId}")
    @ResponseBody
    public Result refund(@PathVariable Long orderId) {
        boolean flag =  alipayService.refund(orderId);

        return Result.ok(flag);
    }

    @GetMapping("closePay/{orderId}")
    @ResponseBody
    public Boolean colsePay(@PathVariable Long orderId) {
        Boolean flag = alipayService.closePay(orderId);
        return flag;
    }

    @RequestMapping("checkPayment/{orderId}")
    @ResponseBody
    public Boolean checkPayment(@PathVariable Long orderId) {
        Boolean flag = alipayService.checkPayment(orderId);
        return flag;
    }

    @GetMapping("getPaymentInfo/{outTradeNo}")
    @ResponseBody
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo) {
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        if (paymentInfo != null) {
            return paymentInfo;
        }
        return null;
    }
}
