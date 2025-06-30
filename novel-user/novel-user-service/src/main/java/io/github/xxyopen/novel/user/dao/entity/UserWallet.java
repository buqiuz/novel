package io.github.xxyopen.novel.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_wallet")
public class UserWallet implements Serializable {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Long goldBalance;
    private String alipayAccount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
