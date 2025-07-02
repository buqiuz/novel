package io.github.xxyopen.novel.payment.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户钱包流水日志实体类
 */
@Data
@TableName("wallet_log")
public class WalletLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 金额变动（正数代表入账，负数代表支出）
     */
    private Long amount;

    /**
     * 资金变动名目（如：充值、消费、退款、转账等）
     */
    private String subject;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
