package io.github.xxyopen.novel.payment.service;

import java.math.BigDecimal;
import io.github.xxyopen.novel.common.resp.RestResp;
import java.util.Map;

public interface PaymentService {
    // 使用支付宝进行支付
    String toPay(Long userId, BigDecimal money);
    // 处理支付宝回调
    void processAlipayPaymentCallback(String encodedUserId,Map<String, String> params);
    // 查询金币余额
    Long getGoldBalance(Long userId);
    // 使用金币
    RestResp <Integer> useGold(Long userId,Long chapterId,Long goldCoins);

}
