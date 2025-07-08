package com.maxiaowei.it.toolbox.controller;

import com.maxiaowei.it.toolbox.service.JsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 功能描述:
 * <p>
 * 作者: maxiaowei
 */
@Controller
@RequestMapping("/json")
public class JsonController {

    private final JsonService jsonService;

    @Autowired
    public JsonController(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @GetMapping
    public String jsonForm() {
        return "json";
    }

    @PostMapping("/format")
    public String formatJson(@RequestParam String jsonInput, Model model) {
        try {
            String formattedJson = jsonService.formatJson(jsonInput);
            model.addAttribute("formattedJson", formattedJson);
        } catch (Exception e) {
            String errorMsg = "JSON格式错误: " + e.getMessage();
            if (e instanceof com.fasterxml.jackson.core.JsonProcessingException) {
                com.fasterxml.jackson.core.JsonProcessingException jpe =
                        (com.fasterxml.jackson.core.JsonProcessingException) e;
                if (jpe.getLocation() != null) {
                    errorMsg += " (位置: 行 " + jpe.getLocation().getLineNr() +
                            ", 列 " + jpe.getLocation().getColumnNr() + ")";
                }
            }
            model.addAttribute("error", errorMsg);
        }
        model.addAttribute("originalJson", jsonInput);
        return "json";
    }
}
