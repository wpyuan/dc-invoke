package com.github.dc.invoke.pojo;

import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *     默认接口日志处理
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/6/9 16:03
 */
@Slf4j
public class DefaultApiLogDataHandler implements IApiLogDataHandler {

    @Override
    public void handle(ApiLogData apiLogData) {
        log.trace("---> 接口请求日志：{}", apiLogData);
    }
}
