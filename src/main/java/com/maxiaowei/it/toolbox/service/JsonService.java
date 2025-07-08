package com.maxiaowei.it.toolbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

/**
 * 功能描述: JSON处理服务
 * <p>
 * 作者: maxiaowei
 */
@Service
public class JsonService {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public String formatJson(String jsonString) throws JsonProcessingException {
        // 尝试自动修复简单格式问题
        String fixedJson = tryFixJson(jsonString);

        Object json = objectMapper.readValue(fixedJson, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    private String tryFixJson(String jsonStr) {
        jsonStr = jsonStr.trim();
        if (!jsonStr.startsWith("{") && !jsonStr.endsWith("}")) {
            return "{" + jsonStr + "}";
        } else if (!jsonStr.startsWith("{")) {
            return "{" + jsonStr;
        } else if (!jsonStr.endsWith("}")) {
            return jsonStr + "}";
        }
        return jsonStr;
    }
}
