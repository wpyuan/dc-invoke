package com.github.dc.invoke.resttemplate.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import com.github.dc.invoke.helper.ApplicationContextHelper;
import com.github.dc.invoke.pojo.ApiLogData;
import com.github.dc.invoke.pojo.DefaultApiLogDataHandler;
import com.github.dc.invoke.util.ApiLogSetupHelper;
import com.github.dc.invoke.util.IpAddressUtil;
import com.github.dc.invoke.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 接口请求调用拦截器
 * </p>
 *
 * @author wpyuan 2020/04/16 9:21
 */
@Slf4j
@Component
public class DefaultClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] bytes,
                                        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        URI uri = request.getURI();
        if (log.isTraceEnabled()) {
            log.trace("=========>>>> start 接口请求>>>> {} \"{}\", headers: {}, bytes: {}", request.getMethod(),
                    uri, request.getHeaders(), new String(bytes));
        }

        Object businessKey = ApiLogSetupHelper.getBusinessKey();
        String apiCode = ApiLogSetupHelper.getApiCode();
        String apiDesc = ApiLogSetupHelper.getApiDesc();
        Integer bodyMaxLength = ApiLogSetupHelper.getBodyMaxLength();
        Charset requestBodyEncoding = ApiLogSetupHelper.getRequestBodyEncoding();
        Charset responseBodyEncoding = ApiLogSetupHelper.getResponseBodyEncoding();
        Class<? extends IApiLogDataHandler> logHandler = ApiLogSetupHelper.getHandler();
        Map<String, Object> context = ApiLogSetupHelper.getContext();
        String ip = IpAddressUtil.getIp();
        String contentType = ObjectUtils.defaultIfNull(request.getHeaders().getContentType(), "").toString();
        String body = null;
        try {
            body = URLDecoder.decode(new String(bytes), ObjectUtils.defaultIfNull(requestBodyEncoding, StandardCharsets.UTF_8).name());
        } catch (Exception e) {
            body = new String(bytes);
        }

        ApiLogData apiLogData = ApiLogData.builder()
                .businessKey(businessKey)
                .apiCode(StringUtils.defaultIfBlank(apiCode, "缺省"))
                .apiDesc(StringUtils.defaultIfBlank(apiDesc, "缺省"))
                .url(uri.getScheme() + "://" + uri.getAuthority() + uri.getPath())
                .method(String.valueOf(request.getMethod()))
                .ip(ip)
                .requestHeaders(JSON.toJSONString(request.getHeaders()))
                .requestQuery(uri.getQuery())
                .requestBody(bodyMaxLength == null || bodyMaxLength > body.length() ? body : body.substring(0, bodyMaxLength))
                .requestContentType(contentType)
                .isInner(false)
                .requestDate(new Date())
                .context(context)
                .build();

        Long startTime = System.currentTimeMillis();
        ClientHttpResponse response = null;
        try {
            response = clientHttpRequestExecution.execute(request, bytes);
            String responseContent = IOUtils.toString(response.getBody(), ObjectUtils.defaultIfNull(responseBodyEncoding, StandardCharsets.UTF_8));
            if (log.isTraceEnabled()) {
                log.trace("=========<<<< end 接口请求<<<< 耗时: {}ms {}/{}, {} \"{}\" 返回body: {}", (System.currentTimeMillis() - startTime),
                        response.getStatusCode(), response.getStatusText(), request.getMethod(),
                        uri, responseContent);
            }
            apiLogData = apiLogData.toBuilder()
                    .isSuccess(response.getStatusCode().is2xxSuccessful())
                    .responseContent(responseContent)
                    .responseCode(response.getStatusCode() + "/" + response.getStatusText())
                    .responseHeaders(JSONObject.toJSONString(response.getHeaders()))
                    .build();
        } catch (Exception e) {
            apiLogData = apiLogData.toBuilder()
                    .isSuccess(false)
                    .exceptionStack(StringUtils.join(ExceptionUtils.getRootCauseStackTrace(e), StringUtils.LF))
                    .build();
            throw e;
        } finally {
            try {
                apiLogData = apiLogData.toBuilder()
                        .consumeTime(System.currentTimeMillis() - startTime)
                        .build();
                IApiLogDataHandler handler = this.getHandler(logHandler);
                handler.handle(apiLogData);
            } catch (Exception e) {
                log.warn("接口日志记录异常", e);
            } finally {
                ApiLogSetupHelper.clear();
            }
        }

        return response;
    }

    /**
     * 获取处理器，优先从获取spring bean，如没有则是单纯的对象，不含注入bean
     * @param handleClazz 处理器class
     * @return 处理器
     */
    public IApiLogDataHandler getHandler(Class<? extends IApiLogDataHandler> handleClazz) {
        if (handleClazz == null) {
            return new DefaultApiLogDataHandler();
        }
        IApiLogDataHandler apiLogDataHandler = ApplicationContextHelper.getBean(handleClazz);
        if (apiLogDataHandler != null) {
            return apiLogDataHandler;
        }
        return ReflectUtil.instance(handleClazz);
    }
}
