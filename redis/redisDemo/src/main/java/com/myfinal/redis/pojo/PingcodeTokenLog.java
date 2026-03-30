package com.myfinal.redis.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * PingCode token获取记录表
 * @TableName pingcode_token_log
 */
@TableName(value ="pingcode_token_log")
@Data
public class PingcodeTokenLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求唯一ID，便于排查日志
     */
    private String requestId;

    /**
     * token来源，如 pingcode_openapi / scheduled_task / manual_refresh
     */
    private String source;

    /**
     * 调用方应用名
     */
    private String appName;

    /**
     * 第三方平台客户端ID
     */
    private String clientId;

    /**
     * token哈希值，避免明文存储
     */
    private String tokenHash;

    /**
     * token前缀，便于人工排查
     */
    private String tokenPrefix;

    /**
     * 获取状态：1成功，0失败
     */
    private Integer acquireStatus;

    /**
     * 失败错误码
     */
    private String errorCode;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 获取时间
     */
    private LocalDateTime acquiredAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 有效期（秒）
     */
    private Integer expiresIn;

    /**
     * 接口HTTP状态码
     */
    private Integer httpStatus;

    /**
     * 接口响应体，可按需裁剪保存
     */
    private String responseBody;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}