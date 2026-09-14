package com.github.dc.invoke.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * RestTemplate 配置类
 * </p>
 *
 * @author wpyuan 2020/04/15 14:53
 */
@Data
@ConfigurationProperties(prefix = "rest-template")
public class RestTemplateProperty {
    private static final int DEFAULT_CONNECT_TIMEOUT = 60000;
    private static final int DEFAULT_READ_TIMEOUT = 60000;
    /**
     * 接口日志记录响应体最大长度（字节），超过此长度只记录前N个字节，防止大响应体导致OOM
     * 默认 1MB
     */
    private static final int DEFAULT_RESPONSE_BODY_LOG_MAX_LENGTH = 1048576;

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    /**
     * 接口日志中响应体最大记录长度（字节），-1 表示不限制（不推荐）
     */
    private int responseBodyLogMaxLength = DEFAULT_RESPONSE_BODY_LOG_MAX_LENGTH;

}