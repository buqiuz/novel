package io.github.xxyopen.novel.payment.controller.front;

import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.payment.dao.entity.WalletLog;
import io.github.xxyopen.novel.payment.dto.resp.WalletLogRespDto;
import io.github.xxyopen.novel.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Tag(name = "FrontPaymentController", description = "前台门户-支付模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_PAYMENT_URL_PREFIX)
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${novel.front.home-url}")
    private String homeUrl;
    /**
     * 支付接口
     */
    @PostMapping("/toPay")
    public String toPay(
            @RequestParam("userId") Long userId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("chapterId") Long chapterId,
            @RequestParam("money") BigDecimal money) {
        return paymentService.toPay(userId,money,bookId,chapterId);
    }
    /**
     * 支付宝支付回调
     */
    @GetMapping("/alipay/return/{encodedUserId}/{encodedBookId}/{encodedChapterId}")
    public String alipayReturn(
            @PathVariable String encodedUserId,
            @PathVariable String encodedBookId,
            @PathVariable String encodedChapterId,
            @RequestParam Map<String, String> params) {
        paymentService.processAlipayPaymentCallback(encodedUserId,params);
        byte[] decodedBookBytes = Base64.getUrlDecoder().decode(encodedBookId);
        Long bookId = Long.valueOf(new String(decodedBookBytes, StandardCharsets.UTF_8));
        byte[] decodedChapterBytes = Base64.getUrlDecoder().decode(encodedChapterId);
        Long chapterId = Long.valueOf(new String(decodedChapterBytes, StandardCharsets.UTF_8));
        System.out.println("书籍ID"+bookId);
        System.out.println("章节ID"+chapterId);
        if(bookId != 0 && chapterId != 0){
            return generateRedirectPage(homeUrl+"/#/book/"+bookId+"/"+chapterId);
        }
        return generateRedirectPage(homeUrl);
    }
    private String generateRedirectPage(String redirectUrl) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>支付结果</title>\n" +
                "    <script type=\"text/javascript\">\n" +
                "        window.location.href = '" + redirectUrl + "';\n" +
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
    /**
     * 获取流水记录
     */
    @GetMapping("/walletLog")
    public RestResp<WalletLogRespDto> getWalletLog(
            @RequestParam("userId") Long userId,
            @RequestParam("pageNum") Long pageNum,
            @RequestParam("pageSize") Long pageSize ){
        return paymentService.getWalletLog(userId,pageNum,pageSize);
    }
}
