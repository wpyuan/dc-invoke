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

    /**
     * 连接池最多保留的空闲连接数，与 okhttp 默认值一致
     */
    private static final int DEFAULT_POOL_MAX_IDLE_CONNECTIONS = 5;

    /**
     * 连接池空闲连接保留时长（秒）。**必须小于服务端的 keepalive_timeout**，
     * 否则会复用一条服务端已关闭的连接，表现为偶发
     * {@code java.io.IOException: unexpected end of stream on http://...}
     */
    private static final int DEFAULT_POOL_KEEP_ALIVE_SECONDS = 30;

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    /**
     * 接口日志中响应体最大记录长度（字节），-1 表示不限制（不推荐）
     */
    private int responseBodyLogMaxLength = DEFAULT_RESPONSE_BODY_LOG_MAX_LENGTH;
    /**
     * 连接池最多保留的空闲连接数
     */
    private int poolMaxIdleConnections = DEFAULT_POOL_MAX_IDLE_CONNECTIONS;
    /**
     * <p>连接池空闲连接保留时长（秒），默认 30 秒。
     *
     * <p><b>这个值必须小于服务端（nginx/Tomcat）的 keepalive_timeout</b>，
     * 否则「距上次调用该主机超过服务端超时时间」的请求，会从连接池里取出一条
     * <b>服务端早已关闭</b>的连接复用，写请求后直接读到 EOF，报
     * {@code unexpected end of stream}。okhttp 默认保留 5 分钟，远大于 nginx 的
     * keepalive_timeout（默认 75s，实测本项目门户为 60s），必然会踩中，故此处收紧到 30 秒。
     *
     * <p>收紧的代价只是多建几次 TCP 连接（内网成本可忽略），换来的是不再复用将死连接；
     * 也不能改成「失败后重试」——发起流程等写操作重试可能产生重复单据。
     */
    private int poolKeepAliveSeconds = DEFAULT_POOL_KEEP_ALIVE_SECONDS;

}