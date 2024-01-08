package com.github.dc.invoke.config;

import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.github.dc.invoke.resttemplate.error.handler.DefaultErrorHandler;
import com.github.dc.invoke.resttemplate.interceptor.DefaultClientHttpRequestInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * <p>
 * RestTemplate 配置类
 * </p>
 *
 * @author wpyuan 2020/04/15 14:53
 */
@Configuration
@DependsOn({"defaultClientHttpRequestInterceptor"})
@EnableConfigurationProperties(RestTemplateProperty.class)
public class RestTemplateConfig {

    @Autowired
    private DefaultClientHttpRequestInterceptor defaultClientHttpRequestInterceptor;

    /**
     * 基于OkHttp3配置RestTemplate
     *
     * @return RestTemplate okHttp客户端
     */
    @Bean("dcRestTemplate")
    public RestTemplate restTemplate(RestTemplateProperty restTemplateProperty) {
        RestTemplate restTemplate = init();
        // 支持https请求，绕过验证
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(HttpsClientHttpRequestFactory.getSocketFactory(), HttpsClientHttpRequestFactory.getX509TrustManager())
                .hostnameVerifier(HttpsClientHttpRequestFactory.getHostnameVerifier())
                .build();
        OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        okHttp3ClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        okHttp3ClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(okHttp3ClientHttpRequestFactory));

        return restTemplate;
    }

    /**
     * 基于HttpURLConnection配置RestTemplate（不缓存请求body，不会出现大文件下载OOM）
     *
     * @param restTemplateProperty
     * @return
     */
    @Bean("downBigFileRestTemplate")
    public RestTemplate downBigFileRestTemplate(RestTemplateProperty restTemplateProperty) {
        RestTemplate restTemplate = init();
        HttpsClientHttpRequestFactory httpsClientHttpRequestFactory = new HttpsClientHttpRequestFactory();
        httpsClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        httpsClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        httpsClientHttpRequestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(httpsClientHttpRequestFactory);
        return restTemplate;
    }

    /**
     * 基于HttpURLConnection配置RestTemplate（不缓存请求body，不会出现大文件上传OOM）
     *
     * @param restTemplateProperty
     * @return
     */
    @Bean("uploadBigFileRestTemplate")
    public RestTemplate uploadBigFileRestTemplate(RestTemplateProperty restTemplateProperty) {
        HttpsClientHttpRequestFactory httpsClientHttpRequestFactory = new HttpsClientHttpRequestFactory();
        httpsClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        httpsClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        httpsClientHttpRequestFactory.setBufferRequestBody(false);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .errorHandler(new DefaultErrorHandler())
                .messageConverters(this.setMessageConverter())
                .requestFactory(() -> httpsClientHttpRequestFactory)
                .build();
        // 不能有拦截器，不然等文件大过运行内存必出现OOM
        return restTemplate;
    }

    private List<HttpMessageConverter<?>> setMessageConverter() {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        fastJsonHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        RestTemplate original = new RestTemplate();
        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();
        httpMessageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        httpMessageConverters.add(fastJsonHttpMessageConverter);
        httpMessageConverters.add(new FormHttpMessageConverter());
        httpMessageConverters.addAll(original.getMessageConverters());
        return httpMessageConverters;
    }

    private RestTemplate init() {
        return new RestTemplateBuilder()
                .errorHandler(new DefaultErrorHandler())
                .interceptors(defaultClientHttpRequestInterceptor)
                .messageConverters(this.setMessageConverter())
                .build();
    }
}

