package com.github.dc.invoke.resttemplate.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import com.github.dc.invoke.config.RestTemplateProperty;
import com.github.dc.invoke.helper.ApplicationContextHelper;
import com.github.dc.invoke.pojo.ApiLogData;
import com.github.dc.invoke.pojo.DefaultApiLogDataHandler;
import com.github.dc.invoke.util.ApiLogSetupHelper;
import com.github.dc.invoke.util.IpAddressUtil;
import com.github.dc.invoke.util.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
@RequiredArgsConstructor
public class DefaultClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final RestTemplateProperty restTemplateProperty;

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
        Boolean isFileUpload = ApiLogSetupHelper.getFileUpload();
        Boolean isFileDownload = ApiLogSetupHelper.getFileDownload();
        String body = null;
        if (BooleanUtils.isNotTrue(isFileUpload)) {
            // 文件上传接口不缓存body，有OOM风险
            try {
                body = URLDecoder.decode(new String(bytes), ObjectUtils.defaultIfNull(requestBodyEncoding, StandardCharsets.UTF_8).name());
            } catch (Exception e) {
                body = new String(bytes);
            }
            body = bodyMaxLength == null || bodyMaxLength > body.length() ? body : body.substring(0, bodyMaxLength);
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
                .requestBody(body)
                .requestContentType(contentType)
                .isInner(false)
                .requestDate(new Date())
                .context(context)
                .build();

        Long startTime = System.currentTimeMillis();
        ClientHttpResponse response = null;
        try {
            response = clientHttpRequestExecution.execute(request, bytes);
            // 下载大文件时不记录响应内容，避免OOM
            if (BooleanUtils.isTrue(isFileDownload)) {
                // 不读取响应体，直接返回
            } else {
                // 读取响应体到字节数组，替代原来的 IOUtils.toString 无限制读取
                // 由于已移除 BufferingClientHttpRequestFactory，需要自行缓存响应体以便调用方重读
                byte[] responseBodyBytes = IOUtils.toByteArray(response.getBody());
                int maxLength = restTemplateProperty.getResponseBodyLogMaxLength();
                String responseContent;
                if (maxLength > 0 && responseBodyBytes.length > maxLength) {
                    // 超过限制长度，只记录前N个字节
                    byte[] truncated = new byte[maxLength];
                    System.arraycopy(responseBodyBytes, 0, truncated, 0, maxLength);
                    responseContent = new String(truncated, ObjectUtils.defaultIfNull(responseBodyEncoding, StandardCharsets.UTF_8))
                            + "...[截断，总大小=" + responseBodyBytes.length + "字节]";
                } else {
                    responseContent = new String(responseBodyBytes, ObjectUtils.defaultIfNull(responseBodyEncoding, StandardCharsets.UTF_8));
                }

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

                // 用缓存的字节数组包装响应，使调用方可以重新读取响应体
                final byte[] cachedBody = responseBodyBytes;
                final ClientHttpResponse originalResponse = response;
                response = new ClientHttpResponse() {
                    @Override
                    public InputStream getBody() throws IOException {
                        return new ByteArrayInputStream(cachedBody);
                    }
                    @Override
                    public HttpHeaders getHeaders() {
                        return originalResponse.getHeaders();
                    }
                    @Override
                    public HttpStatus getStatusCode() throws IOException {
                        return originalResponse.getStatusCode();
                    }
                    @Override
                    public int getRawStatusCode() throws IOException {
                        return originalResponse.getRawStatusCode();
                    }
                    @Override
                    public String getStatusText() throws IOException {
                        return originalResponse.getStatusText();
                    }
                    @Override
                    public void close() {
                        originalResponse.close();
                    }
                };
            }
        } catch (Throwable e) {
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
