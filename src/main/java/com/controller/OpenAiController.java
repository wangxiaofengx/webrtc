package com.controller;

import com.bo.OpenAiResult;
import com.bo.SimpleJsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/openai")
@Slf4j
public class OpenAiController {

    private final RestTemplate restTemplate;

    @Value("${openai.key}")
    String openaiKey;

    public OpenAiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/question")
    public Callable<SimpleJsonResult> index(String prompt, HttpServletRequest request) {
        return () -> {
            Map<String, Object> body = new HashMap<>(16);
            body.put("prompt", prompt);
            body.put("max_tokens", 2048);
            body.put("temperature", 0.5);
            body.put("top_p", 1);
            body.put("frequency_penalty", 0);
            body.put("presence_penalty", 0);
            body.put("model", "text-davinci-003");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("Authorization", "Bearer " + openaiKey);
            HttpEntity httpEntity = new HttpEntity(body, httpHeaders);
            OpenAiResult openAiResult = restTemplate.postForObject("https://api.openai.com/v1/completions", httpEntity, OpenAiResult.class);
            List<OpenAiResult.Choice> choices = openAiResult.getChoices();
            log.info("发送人IP：{}", getIp(request));
            log.info("发送内容：{}", prompt);
            log.info("返回内容：{}", choices.stream().map(OpenAiResult.Choice::getText).collect(Collectors.joining()));
            return SimpleJsonResult.successJsonResult(choices);
        };
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
