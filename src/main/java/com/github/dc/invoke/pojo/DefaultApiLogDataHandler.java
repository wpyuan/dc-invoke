package com.github.dc.invoke.pojo;

import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

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
    public static final String PRINT_RESPONSE = "printResponse";
    private static final String PRINT_BODY = "printBody";
    @Override
    public void handle(ApiLogData apiLogData) {
        log.trace("---> 接口请求日志：{}", apiLogData);
        Map<String, Object> context = ObjectUtils.defaultIfNull(apiLogData.getContext(), new HashMap<>(1));
        boolean printResponse = (boolean) ObjectUtils.defaultIfNull(context.get(PRINT_RESPONSE), true);
        boolean printBody = (boolean) ObjectUtils.defaultIfNull(context.get(PRINT_BODY), true);
        log.info("========> {}调用 {} {} {}", apiLogData.getIsInner() ? "内部接口被".concat(apiLogData.getIp())  : "外部接口", apiLogData.getMethod(), apiLogData.getRequestContentType(), apiLogData.getUrl());
        log.info("\t\t 接口代码： {}, 接口名称：{}", apiLogData.getApiCode(), apiLogData.getApiDesc());
        log.info("\t\t 业务主键： {}", apiLogData.getBusinessKey());
        log.info("\t\t 请求时间： {}", DateFormatUtils.format(apiLogData.getRequestDate(), "yyyy-MM-dd HH:mm:ss"));
        log.info("\t\t headers: {}", apiLogData.getRequestHeaders());
        log.info("\t\t query： {}", apiLogData.getRequestQuery());
        log.info("\t\t body： {}", printBody ? apiLogData.getRequestBody() : "已设置不打印");
        log.info("\t\t 调用是否成功： {}，返回状态码： {}", apiLogData.getIsSuccess(), apiLogData.getResponseCode());
        log.info("\t\t 耗时： {}毫秒", apiLogData.getConsumeTime());
        if (!apiLogData.getIsInner()) {
            log.info("\t\t 返回头：{}", apiLogData.getResponseHeaders());
        }
        if (apiLogData.getIsSuccess()) {
            log.info("\t\t 返回： {}",  printResponse ? apiLogData.getResponseContent() : "已设置不打印");
        } else {
            log.info("\t\t 异常： {}", apiLogData.getExceptionStack());
        }
        log.info("<=====================================================================================================================");
        if (!printBody) {
            apiLogData.setRequestBody("已设置不记录");
        }
        if (!printResponse) {
            apiLogData.setResponseContent("已设置不记录");
        }
    }
}
