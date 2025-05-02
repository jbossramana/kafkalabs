package com.boot.service;
import org.apache.kafka.common.serialization.Serializer;

import com.boot.model.UserBean;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserBeanSerializer implements Serializer<UserBean> {

    @Override
    public byte[] serialize(String topic, UserBean data) {
        return data.toJson().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }
}

