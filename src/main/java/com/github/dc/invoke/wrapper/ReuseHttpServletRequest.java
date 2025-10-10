package com.github.dc.invoke.wrapper;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * 同时在jar包运行时加上字符编码指定为UTF-8：-Dfile.encoding=UTF-8
 *
 * @author PeiYuan
 */
@Getter
public class ReuseHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] rawBody;
    private String body;
    private final ByteArrayInputStream cachedInputStream;
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public ReuseHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.rawBody = IOUtils.toByteArray(request.getInputStream());
        this.cachedInputStream = new ByteArrayInputStream(this.rawBody);
        if (isFormData(request.getContentType())) {
            this.body = this.getFormBodyString(request);
        } else {
            this.body = new String(this.rawBody, StandardCharsets.UTF_8);
        }
    }

    protected String getFormBodyString(final HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (!isFormData(contentType)) return null;
        String bodyString = "";
        StringBuilder sb = new StringBuilder();
        // 表单数据处理逻辑优化
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> next : parameterMap.entrySet()) {
            String[] values = next.getValue();
            String value = null;
            if (values != null) {
                if (values.length == 1) {
                    value = values[0];
                } else {
                    value = Arrays.toString(values);
                }
            }
            sb.append(next.getKey()).append("=").append(value).append("&");
        }
        if (sb.length() > 0) {
            bodyString = sb.toString().substring(0, sb.toString().length() - 1);
        }
        return bodyString;
    }

    private boolean isFormData(String contentType) {
        return StringUtils.isNotBlank(contentType) && (contentType.contains("multipart/form-data") || contentType.contains("application/x-www-form-urlencoded"));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return cachedInputStream.available() == 0;
            }
            @Override
            public boolean isReady() {
                // 输入流始终可读
                return true;
            }
            @Override
            public void setReadListener(ReadListener readListener) {
                // 可选实现
            }
            @Override
            public int read() throws IOException {
                return cachedInputStream.read();
            }
        };
        return servletInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }

}
