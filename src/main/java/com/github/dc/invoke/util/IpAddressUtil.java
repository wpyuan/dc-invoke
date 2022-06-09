package com.github.dc.invoke.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

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
            return getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("获取当前服务器ip异常");
        }
        return null;
    }
}
