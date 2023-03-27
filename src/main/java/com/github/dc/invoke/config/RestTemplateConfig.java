package com.github.dc.invoke.config;

import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.github.dc.invoke.resttemplate.error.handler.DefaultErrorHandler;
import com.github.dc.invoke.resttemplate.interceptor.DefaultClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
     * @return RestTemplate okHttp客户端
     */
    @Bean("dcRestTemplate")
    public RestTemplate restTemplate(RestTemplateProperty restTemplateProperty) {
        RestTemplate restTemplate = init();
        OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory();
        okHttp3ClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        okHttp3ClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(okHttp3ClientHttpRequestFactory));

        return restTemplate;
    }

    /**
     * 基于HttpURLConnection配置RestTemplate（不缓存请求body，不会出现大文件上传OOM）
     * @param restTemplateProperty
     * @return
     */
    @Bean("noCacheBodyRestTemplate")
    public RestTemplate noCacheBodyRestTemplate(RestTemplateProperty restTemplateProperty) {
        RestTemplate restTemplate = init();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        simpleClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        simpleClientHttpRequestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);

        return restTemplate;
    }

    private RestTemplate init() {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        fastJsonHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        RestTemplate original = new RestTemplate();
        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();
        httpMessageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        httpMessageConverters.add(fastJsonHttpMessageConverter);
        httpMessageConverters.add(new FormHttpMessageConverter());
        httpMessageConverters.addAll(original.getMessageConverters());
        return new RestTemplateBuilder()
                .errorHandler(new DefaultErrorHandler())
                .interceptors(defaultClientHttpRequestInterceptor)
                .messageConverters(httpMessageConverters)
                .build();
    }

}

