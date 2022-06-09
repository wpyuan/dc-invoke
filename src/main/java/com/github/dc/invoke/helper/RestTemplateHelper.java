package com.github.dc.invoke.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStream;

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



}
