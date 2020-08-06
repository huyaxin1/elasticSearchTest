package com.test.jdsearchtest.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
public class IndexController {

    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }

}
