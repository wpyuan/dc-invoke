package com.github.dc.invoke.util;

import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import com.github.dc.invoke.pojo.ApiLogData;
import com.github.dc.invoke.pojo.ApiLogSetupMethod;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * <p>
 *     接口日志设置辅助类
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/6/8 10:12
 */
public class ApiLogSetupHelper extends ApiLogSetupMethod {

    public static Object getBusinessKey() {
        return get() == null ? null: get().get(FIELD_BUSINESS_KEY);
    }

    public static String getApiCode() {
        return get() == null ? null: (String) get().get(ApiLogData.FIELD_API_CODE);
    }

    public static String getApiDesc() {
        return get() == null ? null: (String) get().get(ApiLogData.FIELD_API_DESC);
    }

    public static Integer getBodyMaxLength() {
        return get() == null ? null: (Integer) get().get(FIELD_BODY_MAX_LENGTH);
    }

    public static Charset getRequestBodyEncoding() {
        return get() == null ? null: (Charset) get().get(FIELD_REQUEST_ENCODING);
    }

    public static Charset getResponseBodyEncoding() {
        return get() == null ? null: (Charset) get().get(FIELD_ENCODING);
    }

    public static Class<? extends IApiLogDataHandler> getHandler() {
        return get() == null ? null: (Class<? extends IApiLogDataHandler>) get().get(FIELD_HANDLER);
    }

    public static Map<String, Object> getContext() {
        return get() == null ? null: (Map<String, Object>) get().get(FIELD_CONTEXT);
    }
}
