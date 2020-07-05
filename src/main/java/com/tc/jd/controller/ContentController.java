package com.tc.jd.controller;

import com.tc.jd.utils.ContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ContentController {
    @Resource
    private ContentService contentService;

    @RequestMapping("/parse/{keyword}/{page}")
    public Boolean parseContent(@PathVariable("keyword") String keyword,@PathVariable("page") String page) throws IOException {
        return contentService.parseContent(keyword,page);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,
                                           @PathVariable("pageNo") Integer pageNo,
                                           @PathVariable("pageSize") Integer pageSize) throws IOException {
        return contentService.search(keyword,pageNo,pageSize);
    }

    @GetMapping("/searchHigh/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> searchHigh(@PathVariable("keyword") String keyword,
                                           @PathVariable("pageNo") Integer pageNo,
                                           @PathVariable("pageSize") Integer pageSize) throws IOException {
        return contentService.searchHigh(keyword,pageNo,pageSize);
    }
}
