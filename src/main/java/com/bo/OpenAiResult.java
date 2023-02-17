package com.bo;

import lombok.Data;

import java.util.List;

@Data
public class OpenAiResult {

    private String id;

    private String object;

    private Long created;

    private String model;

    private List<Choice> choices;

    @Data
    public static class Choice {
        String text;

        Integer index;

        String logprobs;

        String finish_reason;

    }
}
