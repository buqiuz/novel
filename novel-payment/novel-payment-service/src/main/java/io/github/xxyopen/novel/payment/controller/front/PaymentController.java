package io.github.xxyopen.novel.payment.controller.front;

import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "FrontPaymentController", description = "前台门户-支付模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_PAYMENT_URL_PREFIX)
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    /**
     * 支付接口
     */
    @PostMapping("/toPay")
    public String toPay(
            @RequestParam("userId") Long userId,
            @RequestParam("money") BigDecimal money) {
        return paymentService.toPay(userId, money);
    }
    /**
     * 支付宝支付回调
     */
    @GetMapping("/alipay/return/{encodedUserId}")
    public String alipayReturn(
            @PathVariable String encodedUserId,
            @RequestParam Map<String, String> params) {
        paymentService.processAlipayPaymentCallback(encodedUserId,params);
        return generateRedirectPage();
    }
    private String generateRedirectPage() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>支付结果</title>\n" +
                "    <script type=\"text/javascript\">\n" +
                "        window.location.href = '" + "http://localhost:20000/" + "';\n" +
                "    </script>\n" +
                "</head>\n" +
                "<body>\n" +
                "正在跳转，请稍候...\n" +
                "</body>\n" +
                "</html>";
    }
    /**
     * 获取金币余额
     */
    @GetMapping("/coins")
    public RestResp<Long> getCoins(
            @RequestParam("userId") Long userId ){
        return RestResp.ok(paymentService.getGoldBalance(userId));
    }

    /**
     * 用户使用金币
     */
    @PostMapping("/useCoins")
    public RestResp<Integer> useCoins(
            @RequestParam("userId") Long userId,
            @RequestParam("chapterId") Long chapterId,
            @RequestParam("goldCoins") Long goldCoins ) {
        return paymentService.useGold(userId,chapterId,goldCoins);
    }
}
