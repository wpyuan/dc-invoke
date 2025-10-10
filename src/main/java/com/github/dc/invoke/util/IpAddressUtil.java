package com.github.dc.invoke.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Objects;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/6/8 10:35
 */
@UtilityClass
@Slf4j
public class IpAddressUtil {

    public static InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            log.warn("获取当前服务器ip异常");
        }
        return null;
    }

    public static String getIp() {
        try {
            return Objects.requireNonNull(getLocalHost()).getHostAddress();
        } catch (Exception e) {
            log.warn("获取当前服务器ip异常");
        }
        return null;
    }

    // 优先获取顺序：X-Forwarded-For > X-Real-IP > 代理IP > 本地IP
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个才是真实IP
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            log.trace("X-Real-IP: " + ip);
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理内网IP（如通过Nginx反向代理的情况）
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            return request.getRemoteAddr();
        }
        return ip;
    }
}
