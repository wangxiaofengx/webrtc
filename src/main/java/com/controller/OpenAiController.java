package com.controller;

import com.bo.OpenAiResult;
import com.bo.SimpleJsonResult;
import com.config.OpenAiConfig;
import com.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/openai")
@Slf4j
public class OpenAiController {

    private final RestTemplate restTemplate;

    private final OpenAiConfig openAiConfig;

    public OpenAiController(RestTemplate restTemplate, OpenAiConfig openAiConfig) {
        this.restTemplate = restTemplate;
        this.openAiConfig = openAiConfig;
    }

    private String currDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    private AtomicInteger count = new AtomicInteger(0);

    private static final int thresholdValue = 200;

    @RequestMapping("/question")
    public Callable<SimpleJsonResult> index(String prompt, HttpServletRequest request) {
        return () -> {
            String curData = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            if (StringUtils.equals(this.currDate, curData)) {
                if (count.get() >= thresholdValue) {
                    return SimpleJsonResult.failureJsonResult("当天额度已用完，请明天再来使用，谢谢！");
                }
            } else {
                this.currDate = curData;
                count.set(0);
            }
            count.incrementAndGet();
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
            httpHeaders.set("Authorization", "Bearer " + Stream.of(openAiConfig.getKeys()).findAny().get());
            HttpEntity httpEntity = new HttpEntity(body, httpHeaders);
            log.info("发送人IP：{}", IpUtil.getIp(request));
            log.info("发送内容：{}", prompt);
            OpenAiResult openAiResult = restTemplate.postForObject("https://api.openai.com/v1/completions", httpEntity, OpenAiResult.class);
            List<OpenAiResult.Choice> choices = openAiResult.getChoices();
            log.info("返回内容：{}", choices.stream().map(OpenAiResult.Choice::getText).collect(Collectors.joining()));
            return SimpleJsonResult.successJsonResult(choices);
        };
    }


}
