package io.github.xxyopen.novel.resource.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author xiongxiaoyang
 * @date 2022/05/11
 */
@Setter
@Getter
@TableName("user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录名
     */
    private String username;

    /**
     * 登录密码-加密
     */
    private String password;

    /**
     * 加密盐值
     */
    private String salt;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String userPhoto;

    /**
     * 用户性别;0-男 1-女
     */
    private Integer userSex;

    /**
     * 账户余额
     */
    private Long accountBalance;

    /**
     * 用户状态;0-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    @Override
    public String toString() {
        return "UserInfo{" +
        "id=" + id +
        ", username=" + username +
        ", password=" + password +
        ", salt=" + salt +
        ", nickName=" + nickName +
        ", userPhoto=" + userPhoto +
        ", userSex=" + userSex +
        ", accountBalance=" + accountBalance +
        ", status=" + status +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
