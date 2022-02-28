package com.yarda.elasticsearch.controller;

import com.yarda.elasticsearch.domain.AjaxResult;
import com.yarda.elasticsearch.domain.Doc;
import com.yarda.elasticsearch.service.TestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * elasticsearch测试 controller
 * @author xuezheng
 * @version 1.0
 * @date 2022/2/23 17:11
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private TestService testService;

    /**
     * 获取文档详情--根据id
     */
    @GetMapping("/{id}")
    public AjaxResult query(@PathVariable String id){
        return AjaxResult.success(testService.getDocById(id));
    }

    /**
     * 添加文档
     */
    @PostMapping
    public AjaxResult add(@RequestBody Doc doc){
        return AjaxResult.success(testService.insertDoc(doc));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable String id){
        return AjaxResult.success(testService.deleteDoc(id));
    }

    /**
     * 关键字检索文档
     */
    @GetMapping("/search")
    public AjaxResult search(String searchValue){
        return AjaxResult.success(testService.searchDoc(searchValue));
    }
}
