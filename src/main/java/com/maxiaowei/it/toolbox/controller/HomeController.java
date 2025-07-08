package com.maxiaowei.it.toolbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 功能描述:
 * <p>
 * 作者: maxiaowei
 */

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
