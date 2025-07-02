package io.github.xxyopen.novel.payment.service.impl;

import com.alipay.easysdk.kernel.util.ResponseChecker;
import io.github.xxyopen.novel.book.dto.req.ChapterUnlockReqDto;
import io.github.xxyopen.novel.book.feign.BookFeign;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.payment.dao.entity.UserWallet;
import io.github.xxyopen.novel.payment.dao.entity.WalletLog;
import io.github.xxyopen.novel.payment.dao.mapper.UserWalletMapper;
import io.github.xxyopen.novel.payment.dao.mapper.WalletLogMapper;
import io.github.xxyopen.novel.payment.dto.resp.WalletLogRespDto;
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
import java.util.List;
import java.util.Map;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.factory.Factory.Payment;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import io.github.xxyopen.novel.common.resp.RestResp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private UserWalletMapper userWalletMapper;
    @Autowired
    private WalletLogMapper walletLogMapper;
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
    @Value("${novel.front.home-url}")
    private String home_url;

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

    private void addCoins(Long userId,long  coins,String subject){
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
            //添加钱包日志
            WalletLog log = new WalletLog();
            log.setUserId(userId);
            log.setAmount(coins);
            log.setSubject(subject);
            log.setCreatedAt(LocalDateTime.now());
            walletLogMapper.insert(log);
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
        addCoins(userId, coinsToAdd,"账户充值");
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
                    // 添加钱包日志
                    WalletLog log = new WalletLog();
                    log.setUserId(userId);
                    log.setAmount(-goldCoins);
                    log.setSubject("使用书币解锁章节");
                    log.setCreatedAt(LocalDateTime.now());
                    walletLogMapper.insert(log);
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
    @Override
    public RestResp<WalletLogRespDto> getWalletLog(Long userId, Long pageNum, Long pageSize) {
        try {
            // 创建分页对象，从第 pageNum 页开始，每页显示 pageSize 条记录
            Page<WalletLog> page = new Page<>(pageNum, pageSize);
            // 构造查询条件：根据用户ID查询对应的流水日志
            QueryWrapper<WalletLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            WalletLogRespDto walletLogRespDto = new WalletLogRespDto();
            // 计算总记录数
            walletLogRespDto.setTotal(walletLogMapper.selectCount(queryWrapper));
            // 执行分页查询
            IPage<WalletLog> result = walletLogMapper.selectPage(page, queryWrapper);
            walletLogRespDto.setWalletLogs(result.getRecords());
            // 返回查询结果
            return RestResp.ok(walletLogRespDto);
        } catch (Exception e) {
            System.out.println("查询用户钱包日志异常，原因：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
