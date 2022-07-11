package com.github.dc.invoke.helper;

import com.github.dc.invoke.util.ApiLogSetupHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *     RestTemplate辅助类
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/6/9 15:47
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestTemplateHelper {
    private final RestTemplate dcRestTemplate;

    /**
     * get请求
     * @param url
     * @param responseType
     * @return
     */
    public <R> ResponseEntity<R> get(String url, Class<R> responseType) {
        return dcRestTemplate.getForEntity(url, responseType);
    }

    /**
     * 根据下载链接获取文件字节数组
     * @param url
     * @return
     */
    public ResponseEntity<byte[]> getBytesForEntity(String url) {
        return dcRestTemplate.getForEntity(url, byte[].class);
    }

    /**
     * 根据下载链接获取文件字节数组
     * @param url
     * @return
     */
    public byte[] getBytes(String url) {
        ResponseEntity<byte[]> response = this.getBytesForEntity(url);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        log.warn("下载资源失败，GET {}，response: {}", url, response);
        return null;
    }

    /**
     * 根据下载链接获取文件字节数组并写入输出流
     * @param url
     * @param os
     */
    public void getBytesToOutputStream(String url, OutputStream os) {
        try {
            os.write(this.getBytes(url));
        } catch (IOException e) {
            log.warn("下载资源失败，GET {}" + url, e);
        }
    }

    /**
     * post x-www-form-urlencoded格式内容请求
     * @param url 请求地址
     * @param param 请求内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postFormUrlencoded(String url, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.post(url, MediaType.APPLICATION_FORM_URLENCODED, null, param, responseType);
    }

    /**
     * post x-www-form-urlencoded格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param param 请求内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postFormUrlencoded(String url, HttpHeaders headers, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.post(url, MediaType.APPLICATION_FORM_URLENCODED, headers, param, responseType);
    }

    /**
     * post json格式内容请求
     * @param url 请求地址
     * @param bodyJson 请求内容json字符串
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postJson(String url, String bodyJson, Class<R> responseType) {
        return this.post(url, MediaType.APPLICATION_JSON, null, bodyJson, responseType);
    }

    /**
     * post json格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param bodyJson 请求内容json字符串
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postJson(String url, HttpHeaders headers, String bodyJson, Class<R> responseType) {
        return this.post(url, MediaType.APPLICATION_JSON, headers, bodyJson, responseType);
    }

    /**
     * post form-data格式内容请求
     * @param url 请求地址
     * @param param 请求form内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postFormData(String url, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.post(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), null, param, responseType);
    }

    /**
     * post form-data格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param param 请求form内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postFormData(String url, HttpHeaders headers, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.post(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), headers, param, responseType);
    }

    /**
     * post form-data格式内容请求
     * @param url 请求地址
     * @param param 请求form内容
     * @param fileParamName 请求form的文件变量名
     * @param file 请求form的文件
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postFormData(String url, LinkedMultiValueMap<String, Object> param, String fileParamName, File file, Class<R> responseType) {
        FileSystemResource fileResource = new FileSystemResource(file);
        param.add(fileParamName, fileResource);
        return this.post(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), null, param, responseType);
    }

    /**
     * post form-data格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param param 请求form内容
     * @param fileParamName 请求form的文件变量名
     * @param file 请求form的文件
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> postFormData(String url, HttpHeaders headers, LinkedMultiValueMap<String, Object> param, String fileParamName, File file, Class<R> responseType) {
        FileSystemResource fileResource = new FileSystemResource(file);
        param.add(fileParamName, fileResource);
        return this.post(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), headers, param, responseType);
    }

    /**
     * post请求
     * @param url 请求地址
     * @param contentType 请求contentType
     * @param body 请求body 根据contenType不同，类型不同
     * @param responseType 返回值类型
     * @param <R>
     * @param <B>
     * @return 返回内容
     */
    public <R, B> ResponseEntity<R> post(String url, MediaType contentType, B body, Class<R> responseType) {
        return this.post(url, contentType, null, body, responseType);
    }

    /**
     * post请求
     * @param url 请求地址
     * @param contentType 请求contentType
     * @param headers 请求头
     * @param body 请求body 根据contenType不同，类型不同
     * @param responseType 返回值类型
     * @param <R>
     * @param <B>
     * @return 返回内容
     */
    public <R, B> ResponseEntity<R> post(String url, MediaType contentType, HttpHeaders headers, B body, Class<R> responseType) {
        RequestEntity requestEntity = RequestEntity
                .post(url)
                .contentType(contentType)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .body(body);
        return dcRestTemplate.postForEntity(url, requestEntity, responseType);
    }
}
