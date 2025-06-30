package io.github.xxyopen.novel.payment.service.impl;

import com.alipay.easysdk.kernel.util.ResponseChecker;
import io.github.xxyopen.novel.book.dto.req.ChapterUnlockReqDto;
import io.github.xxyopen.novel.book.feign.BookFeign;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.payment.dao.entity.UserWallet;
import io.github.xxyopen.novel.payment.dao.mapper.UserWalletMapper;
import io.github.xxyopen.novel.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.factory.Factory.Payment;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import io.github.xxyopen.novel.common.resp.RestResp;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private UserWalletMapper userWalletMapper;
    @Autowired
    private BookFeign bookFeign;
    @Value("${alipay.app-id}")
    private String appId;
    @Value("${alipay.private-key}")
    private String privateKey;
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    @Value("${alipay.gateway-url}")
    private String gatewayurl;
    @Value("${alipay.return-url}")
    private String returnUrl;
    @Value("${coin.exchange-rate}")
    private Integer exchangeRate;
    private Config getOptions() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = gatewayurl;
        config.signType = "RSA2";
        config.appId = appId;
        config.merchantPrivateKey = privateKey;
        config.alipayPublicKey = alipayPublicKey;
        config.encryptKey = "";
        config.notifyUrl = returnUrl;
        return config;
    }
    private String generateTradeNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
    }

    private void addCoins(Long userId,long  coins){
        try{
            UserWallet wallet = userWalletMapper.selectById(userId);
            if(wallet == null){
                log.warn("用户钱包不存在，userId={}", userId);
                throw new RuntimeException("用户钱包不存在");
            }
            wallet.setGoldBalance(wallet.getGoldBalance() + coins);
            wallet.setUpdatedTime(LocalDateTime.now());
            int rows = userWalletMapper.updateById(wallet);
            if(rows <= 0){
                log.warn("更新用户钱包失败，userId={}", userId);
                throw new RuntimeException("更新用户钱包失败");
            }
            log.info("用户充值成功，userId={}, 书币数={}", userId, coins);
        } catch (Exception e) {
            System.out.println("添加书币异常，原因："+e.getMessage());
            throw new RuntimeException(e.getMessage(),e);
        }
    }
    @Override
    public String toPay(Long userId, BigDecimal money) {
        // 对用户ID进行Base64编码
        String encodedUserId = Base64.getUrlEncoder().encodeToString(
                String.valueOf(userId).getBytes(StandardCharsets.UTF_8)
        );
        // 动态拼接完整的回调地址
        String finalReturnUrl = String.format(returnUrl, encodedUserId);
        Factory.setOptions(getOptions());
        try{
            AlipayTradePagePayResponse response =Payment.Page().pay("书币充值",this.generateTradeNo(),String.valueOf(money),finalReturnUrl);
            if(ResponseChecker.success(response)){
                return response.getBody();
            }else{
                log.error("支付宝支付失败");
                return null;
            }
        } catch (Exception e) {
            log.error("支付宝支付异常，原因："+e.getMessage());
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    @Override
    public void processAlipayPaymentCallback(String encodedUserId,Map<String, String> params){
        System.out.println("支付宝支付回调");
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedUserId);
        Long userId = Long.valueOf(new String(decodedBytes, StandardCharsets.UTF_8));
        BigDecimal money = new BigDecimal(params.get("total_amount"));
        long coinsToAdd = money.multiply(BigDecimal.valueOf(exchangeRate)).longValueExact();
        addCoins(userId, coinsToAdd);
    }

    @Override
    public Long getGoldBalance(Long userId) {
        try{
            UserWallet wallet = userWalletMapper.selectById(userId);
            return wallet.getGoldBalance();
        } catch (Exception e) {
            System.out.println("查询用户金币余额异常，原因："+e.getMessage());
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp <Integer>  useGold(Long userId,Long chapterId, Long goldCoins) {
        try{
            UserWallet wallet = userWalletMapper.selectById(userId);
            Long goldBalance =wallet.getGoldBalance();
            if(goldBalance < goldCoins){
                return RestResp.ok(0);
            }
            else{
                wallet.setGoldBalance(goldBalance - goldCoins);
                wallet.setUpdatedTime(LocalDateTime.now());
                int rows = userWalletMapper.updateById(wallet);
                if(rows > 0){
                    ChapterUnlockReqDto dto = new ChapterUnlockReqDto();
                    dto.setUserId(userId);
                    dto.setChapterId(chapterId);
                    RestResp<Boolean> resp = bookFeign.insertBookChapterUnlock(dto);
                    if(resp.getData()){
                        return RestResp.ok(1);
                    }
                }
                return RestResp.ok(rows);
            }
        } catch (Exception e) {
            System.out.println("使用书币异常，原因："+e.getMessage());
            throw new RuntimeException(e.getMessage(),e);
        }
    }
}
