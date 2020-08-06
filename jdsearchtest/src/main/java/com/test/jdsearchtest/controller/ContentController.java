package com.test.jdsearchtest.controller;

import com.test.jdsearchtest.service.ContentService;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/create/{keyword}")
    public Boolean create(@PathVariable("keyword") String keyword){
        return contentService.createContent(keyword);
    }

    @GetMapping("/searchPage/{keyword}/{pageNum}/{pageSize}")
    public List<Map<String,Object>> searchPage(
            @PathVariable("keyword") String keyword,
            @PathVariable("pageNum") Integer pageNum,
            @PathVariable("pageSize") Integer pageSize){
        return contentService.searchPage(keyword,pageNum,pageSize);
    }

    @GetMapping("/highlightShow/{keyword}/{pageNum}/{pageSize}")
    public List<Map<String,Object>> highlightShow(
            @PathVariable("keyword") String keyword,
            @PathVariable("pageNum") Integer pageNum,
            @PathVariable("pageSize") Integer pageSize){
        return contentService.highlightShow(keyword,pageNum,pageSize);
    }
}
