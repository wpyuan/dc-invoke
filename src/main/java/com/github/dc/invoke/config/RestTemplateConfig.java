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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
        X509TrustManager manager = getX509TrustManager();
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(getSocketFactory(manager), manager)
                .hostnameVerifier(getHostnameVerifier())
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
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        simpleClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        simpleClientHttpRequestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);

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
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(restTemplateProperty.getReadTimeout());
        simpleClientHttpRequestFactory.setConnectTimeout(restTemplateProperty.getConnectTimeout());
        simpleClientHttpRequestFactory.setBufferRequestBody(false);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        restTemplate.setErrorHandler(new DefaultErrorHandler());
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        // 不能有拦截器，不然等文件大过运行内存必出现OOM
        return restTemplate;
    }

    public static X509TrustManager getX509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    public static SSLSocketFactory getSocketFactory(TrustManager manager) {
        SSLSocketFactory socketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{manager}, new SecureRandom());
            socketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return socketFactory;
    }

    public static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
        return hostnameVerifier;
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

