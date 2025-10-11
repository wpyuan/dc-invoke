package com.github.dc.invoke.aop.handler;


import com.github.dc.invoke.pojo.ApiLogData;

/**
 * api日志数据处理器
 * @author PeiYuan
 */
public interface IApiLogDataHandler {
    /**
     * 根据指定接口代码判断是否启用日志处理。
     * 默认启用
     * @param apiCode
     * @return
     */
    default boolean enable(String apiCode) { return true; };

    /**
     * 日志数据处理
     * @param apiLogData 日志数据
     */
    void handle(ApiLogData apiLogData);
}
