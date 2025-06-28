package io.github.xxyopen.novel.payment.service.impl;

import io.github.xxyopen.novel.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBootTest
public class PaymentServiceImplTest {
    @Autowired
    private PaymentService paymentService;
    @Test
    public void toPay() {
        String payUrl = paymentService.toPay(1L, BigDecimal.valueOf(1));
        System.out.println(payUrl);
    }
    @Test
    public void getGoldBalance() {
        Long goldBalance = paymentService.getGoldBalance(1L);
        System.out.println(goldBalance);
    }
}
