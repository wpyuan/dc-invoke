package com.github.dc.invoke.util;

import com.github.dc.invoke.aop.handler.IApiLogDataHandler;
import com.github.dc.invoke.pojo.ApiLogData;
import com.github.dc.invoke.pojo.ApiLogSetupMethod;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
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

    public static Boolean getFileUpload() {
        return get() == null ? null: (Boolean) get().get(FIELD_FILE_UPLOAD);
    }

    public static Boolean getFileDownload() {
        return get() == null ? null: (Boolean) get().get(FIELD_FILE_DOWNLOAD);
    }

    public static Class<? extends IApiLogDataHandler> getHandler() {
        return get() == null ? null: (Class<? extends IApiLogDataHandler>) get().get(FIELD_HANDLER);
    }

    public static Map<String, Object> getContext() {
        return get() == null ? null: (Map<String, Object>) get().get(FIELD_CONTEXT);
    }

    public static ApiLogSetupHelper.ApiLogSetupBuilder builder() {
        return new ApiLogSetupHelper.ApiLogSetupBuilder();
    }

    public static class ApiLogSetupBuilder {
        private Map<String, Object> apiInfo = new HashMap<>(3);

        public ApiLogSetupHelper.ApiLogSetupBuilder businessKey(Object businessKey) {
            this.apiInfo.put(FIELD_BUSINESS_KEY, businessKey);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder apiCode(String apiCode) {
            this.apiInfo.put(ApiLogData.FIELD_API_CODE, apiCode);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder apiDesc(String apiDesc) {
            this.apiInfo.put(ApiLogData.FIELD_API_DESC, apiDesc);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder bodyMaxLength(Integer bodyMaxLength) {
            this.apiInfo.put(FIELD_BODY_MAX_LENGTH, bodyMaxLength);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder requestBodyEncoding(Charset requestBodyEncoding) {
            this.apiInfo.put(FIELD_REQUEST_ENCODING, requestBodyEncoding);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder responseBodyEncoding(Charset responseBodyEncoding) {
            this.apiInfo.put(FIELD_ENCODING, responseBodyEncoding);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder fileUpload(Boolean fileUpload) {
            this.apiInfo.put(FIELD_FILE_UPLOAD, fileUpload);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder fileDownload(Boolean fileDownload) {
            this.apiInfo.put(FIELD_FILE_DOWNLOAD, fileDownload);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder handler(Class<? extends IApiLogDataHandler> handler) {
            this.apiInfo.put(FIELD_HANDLER, handler);
            return this;
        }

        public ApiLogSetupHelper.ApiLogSetupBuilder context(Map<String, Object> context) {
            this.apiInfo.put(FIELD_CONTEXT, context);
            return this;
        }

        public void build() {
            ApiLogSetupHelper.set(this.apiInfo);
        }

    }

}
