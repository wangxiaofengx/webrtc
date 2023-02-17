package com.bo;


import lombok.Data;

@Data
public class SimpleJsonResult {
    private boolean success;
    private Object data;

    public SimpleJsonResult(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public static SimpleJsonResult successJsonResult(Object data) {
        return new SimpleJsonResult(true, data);
    }

    public static SimpleJsonResult failureJsonResult(Object data) {
        return new SimpleJsonResult(false, data);
    }
}
