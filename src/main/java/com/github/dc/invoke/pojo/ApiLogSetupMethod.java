package com.github.dc.invoke.pojo;

import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import com.github.dc.invoke.util.ApiLogSetupHelper;
import org.apache.commons.lang3.ObjectUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     接口日志设置方法
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/6/8 10:16
 */
public abstract class ApiLogSetupMethod {
    protected static final ThreadLocal<Map<String, Object>> LOCAL_SETUP = new ThreadLocal<>();
    public static final String FIELD_CONTEXT = "context";
    public static final String FIELD_BUSINESS_KEY = "businessKey";
    public static final String FIELD_HANDLER = "handler";
    public static final String FIELD_ENCODING = "encoding";
    public static final String FIELD_REQUEST_ENCODING = "requestEncoding";
    public static final String FIELD_BODY_MAX_LENGTH = "bodyMaxLength";
    public static final String FIELD_FILE_UPLOAD = "fileUpload";
    public static final String FIELD_FILE_DOWNLOAD = "fileDownload";

    public static void set(String apiCode, String apiDesc, Class<? extends IApiLogDataHandler> handler) {
        set(null, apiCode, apiDesc, null, StandardCharsets.UTF_8, null, handler, null);
    }

    public static void set(Object businessKey, String apiCode, String apiDesc, Class<? extends IApiLogDataHandler> handler) {
        set(businessKey, apiCode, apiDesc, null, StandardCharsets.UTF_8, null, handler, null);
    }

    public static void set(Object businessKey, String apiCode, String apiDesc, Class<? extends IApiLogDataHandler> handler, Map<String, Object> context) {
        set(businessKey, apiCode, apiDesc, null, StandardCharsets.UTF_8, null, handler, context);
    }

    public static void set(Object businessKey, String apiCode, String apiDesc, Integer bodyMaxLength, Class<? extends IApiLogDataHandler> handler, Map<String, Object> context) {
        set(businessKey, apiCode, apiDesc, bodyMaxLength, StandardCharsets.UTF_8, null, handler, context);
    }

    public static void set(Object businessKey, String apiCode, String apiDesc, Charset responseBodyEncoding, Class<? extends IApiLogDataHandler> handler, Map<String, Object> context) {
        set(businessKey, apiCode, apiDesc, null, StandardCharsets.UTF_8, responseBodyEncoding, handler, context);
    }

    public static void set(Object businessKey, String apiCode, String apiDesc, Integer bodyMaxLength, Charset responseBodyEncoding, Class<? extends IApiLogDataHandler> handler, Map<String, Object> context) {
        set(businessKey, apiCode, apiDesc, bodyMaxLength, StandardCharsets.UTF_8, responseBodyEncoding, handler, context);
    }

    public static void set(Object businessKey, String apiCode, String apiDesc, Integer bodyMaxLength, Charset requestBodyEncoding, Charset responseBodyEncoding, Class<? extends IApiLogDataHandler> handler, Map<String, Object> context) {
        Map<String, Object> apiInfo = new HashMap<>(3);
        apiInfo.put(FIELD_BUSINESS_KEY, businessKey);
        apiInfo.put(ApiLogData.FIELD_API_CODE, apiCode);
        apiInfo.put(ApiLogData.FIELD_API_DESC, apiDesc);
        apiInfo.put(FIELD_BODY_MAX_LENGTH, bodyMaxLength);
        apiInfo.put(FIELD_REQUEST_ENCODING, requestBodyEncoding);
        apiInfo.put(FIELD_ENCODING, responseBodyEncoding);
        apiInfo.put(FIELD_HANDLER, handler);
        apiInfo.put(FIELD_CONTEXT, context);
        LOCAL_SETUP.set(apiInfo);
    }

    public static void set(Map<String, Object> data) {
        LOCAL_SETUP.set(data);
    }

    public static void append(Map<String, Object> data) {
        LOCAL_SETUP.get().putAll(data);
    }

    public static Map<String, Object> get() {
        return LOCAL_SETUP.get();
    }

    public static void clear() {
        LOCAL_SETUP.remove();
    }
}
