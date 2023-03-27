package com.github.dc.invoke.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

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
    private final RestTemplate noCacheBodyRestTemplate;

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
     * get请求
     * @param url
     * @param headers
     * @param responseType
     * @return
     */
    public <R> ResponseEntity<R> get(String url, HttpHeaders headers, Class<R> responseType) {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        return dcRestTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);
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
     * 根据下载链接获取文件字节数组
     * @param url
     * @param headers
     * @return
     */
    public byte[] getBytes(String url, HttpHeaders headers) {
        ResponseEntity<byte[]> response = this.getBytesForEntity(url, headers);
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
     * 根据下载链接获取文件字节数组并写入输出流
     * @param url
     * @param headers
     * @param os
     */
    public void getBytesToOutputStream(String url, HttpHeaders headers, OutputStream os) {
        try {
            os.write(this.getBytes(url, headers));
        } catch (IOException e) {
            log.warn("下载资源失败，GET {}" + url, e);
        }
    }

    /**
     * 根据下载链接获取文件字节数组
     * @param url
     * @return
     */
    public ResponseEntity<byte[]> getBytesForEntity(String url) {
        return this.getBytesForEntity(url, new HttpHeaders());
    }

    /**
     * 根据下载链接获取文件字节数组
     * @param url
     * @param headers
     * @return
     */
    public ResponseEntity<byte[]> getBytesForEntity(String url, HttpHeaders headers) {
        /**
         * 对响应进行流式处理而不是将其全部加载到内存中
         * 设置了请求头APPLICATION_OCTET_STREAM，表示以流的形式进行数据加载
         */
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        RequestCallback requestCallback = dcRestTemplate.httpEntityCallback(httpEntity);
        File responseFile = dcRestTemplate.execute(url, HttpMethod.GET, requestCallback, clientHttpResponse -> {
            File tempFile = File.createTempFile("download", ".tmp");
            try (InputStream inputStream = clientHttpResponse.getBody()){
                Files.copy(inputStream, Paths.get(tempFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        });
        try {
            byte[] fileBytes = FileUtils.readFileToByteArray(responseFile);
            return ResponseEntity.ok(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("文件下载成功后保存至本地临时文件发生异常", e);
        } finally {
            // 删除临时文件
            responseFile.delete();
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
        RequestEntity requestEntity = RequestEntity
                .post(url)
                .contentType(MediaType.parseMediaType("multipart/form-data; charset=UTF-8"))
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers((HttpHeaders) null)
                .body(param);
        return noCacheBodyRestTemplate.postForEntity(url, requestEntity, responseType);
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
        RequestEntity requestEntity = RequestEntity
                .post(url)
                .contentType(MediaType.parseMediaType("multipart/form-data; charset=UTF-8"))
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .body(param);
        return noCacheBodyRestTemplate.postForEntity(url, requestEntity, responseType);
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

    /**
     * delete请求
     * @param url 请求地址
     */
    public void delete(String url) {
        dcRestTemplate.delete(url);
    }

    /**
     * delete请求
     * @param url 请求地址
     * @param headers 请求头
     * @return 返回内容
     */
    public void delete(String url, HttpHeaders headers) {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        dcRestTemplate.exchange(url, HttpMethod.DELETE, httpEntity, void.class);
    }

    /**
     * delete请求
     * @param url 请求地址
     * @param responseType 返回类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> delete(String url, Class<R> responseType) {
        RequestEntity requestEntity = RequestEntity
                .delete(url)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .build();
        return dcRestTemplate.exchange(requestEntity, responseType);
    }

    /**
     * delete请求
     * @param url 请求地址
     * @param headers 请求头
     * @param responseType 返回类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> delete(String url, HttpHeaders headers, Class<R> responseType) {
        RequestEntity requestEntity = RequestEntity
                .delete(url)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .build();
        return dcRestTemplate.exchange(requestEntity, responseType);
    }

    /**
     * put x-www-form-urlencoded格式内容请求
     * @param url 请求地址
     * @param param 请求内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putFormUrlencoded(String url, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.put(url, MediaType.APPLICATION_FORM_URLENCODED, null, param, responseType);
    }

    /**
     * put x-www-form-urlencoded格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param param 请求内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putFormUrlencoded(String url, HttpHeaders headers, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.put(url, MediaType.APPLICATION_FORM_URLENCODED, headers, param, responseType);
    }

    /**
     * put json格式内容请求
     * @param url 请求地址
     * @param bodyJson 请求内容json字符串
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putJson(String url, String bodyJson, Class<R> responseType) {
        return this.put(url, MediaType.APPLICATION_JSON, null, bodyJson, responseType);
    }

    /**
     * put json格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param bodyJson 请求内容json字符串
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putJson(String url, HttpHeaders headers, String bodyJson, Class<R> responseType) {
        return this.put(url, MediaType.APPLICATION_JSON, headers, bodyJson, responseType);
    }

    /**
     * put form-data格式内容请求
     * @param url 请求地址
     * @param param 请求form内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putFormData(String url, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.put(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), null, param, responseType);
    }

    /**
     * put form-data格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param param 请求form内容
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putFormData(String url, HttpHeaders headers, LinkedMultiValueMap<String, Object> param, Class<R> responseType) {
        return this.put(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), headers, param, responseType);
    }

    /**
     * put form-data格式内容请求
     * @param url 请求地址
     * @param param 请求form内容
     * @param fileParamName 请求form的文件变量名
     * @param file 请求form的文件
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putFormData(String url, LinkedMultiValueMap<String, Object> param, String fileParamName, File file, Class<R> responseType) {
        FileSystemResource fileResource = new FileSystemResource(file);
        param.add(fileParamName, fileResource);
        return this.put(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), null, param, responseType);
    }

    /**
     * put form-data格式内容请求
     * @param url 请求地址
     * @param headers 请求头
     * @param param 请求form内容
     * @param fileParamName 请求form的文件变量名
     * @param file 请求form的文件
     * @param responseType 返回值类型
     * @param <R>
     * @return 返回内容
     */
    public <R> ResponseEntity<R> putFormData(String url, HttpHeaders headers, LinkedMultiValueMap<String, Object> param, String fileParamName, File file, Class<R> responseType) {
        FileSystemResource fileResource = new FileSystemResource(file);
        param.add(fileParamName, fileResource);
        return this.put(url, MediaType.parseMediaType("multipart/form-data; charset=UTF-8"), headers, param, responseType);
    }

    /**
     * put请求
     * @param url 请求地址
     * @param contentType 请求contentType
     * @param body 请求body 根据contenType不同，类型不同
     * @param responseType 返回值类型
     * @param <R>
     * @param <B>
     * @return 返回内容
     */
    public <R, B> ResponseEntity<R> put(String url, MediaType contentType, B body, Class<R> responseType) {
        return this.put(url, contentType, null, body, responseType);
    }

    /**
     * put请求
     * @param url 请求地址
     * @param contentType 请求contentType
     * @param headers 请求头
     * @param body 请求body 根据contenType不同，类型不同
     * @param responseType 返回值类型
     * @param <R>
     * @param <B>
     * @return 返回内容
     */
    public <R, B> ResponseEntity<R> put(String url, MediaType contentType, HttpHeaders headers, B body, Class<R> responseType) {
        RequestEntity requestEntity = RequestEntity
                .put(url)
                .contentType(contentType)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .body(body);
        return dcRestTemplate.exchange(requestEntity, responseType);
    }

    /**
     * 实例，用于调用原有未封装的方法
     * @return
     */
    public RestTemplate instance() {
        return this.dcRestTemplate;
    }
}
