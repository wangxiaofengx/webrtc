package com.config;

import com.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getInterceptors().add((request, body, execution) -> {
            ClientHttpResponse execute = execution.execute(request, body);
            HttpStatus statusCode = execute.getStatusCode();
            HttpStatus.Series series = statusCode.series();

            if (series != HttpStatus.Series.CLIENT_ERROR && series != HttpStatus.Series.SERVER_ERROR) {
                return execute;
            }
            String result = "";
            MediaType contentType = request.getHeaders().getContentType();
            if (request.getMethod().equals(HttpMethod.GET)
                    || MediaType.APPLICATION_JSON.equals(contentType)
                    || MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)
                    || MediaType.TEXT_PLAIN.equals(contentType)
            ) {
                if (series == HttpStatus.Series.SERVER_ERROR) {
                    result = StreamUtils.copyToString(execute.getBody(), StandardCharsets.UTF_8);
                }
                log.error("请求地址：{}", URLDecoder.decode(request.getURI().toString(), "utf-8"));
                log.error("状态码：{}", statusCode.value());
                log.error("请求参数：{}", new String(body));
                log.error("返回结果：{}", result);
            }
            throw new RuntimeException(statusCode.value() + " " + execute.getStatusText() + (StringUtils.isBlank(result) ? "" : ": [" + result + "]"));
        });
        return restTemplate;
    }
}
