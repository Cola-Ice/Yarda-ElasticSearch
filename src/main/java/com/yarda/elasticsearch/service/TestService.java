package com.yarda.elasticsearch.service;

import com.yarda.elasticsearch.domain.Doc;

import java.util.List;

/**
 * elasticsearch测试 service
 * @author xuezheng
 * @version 1.0
 * @date 2022/2/23 17:28
 */
public interface TestService {
    /**
     * 获取文档详情--根据id
     * @param id 要查询的文档id
     * @return 文档详情
     */
    Doc getDocById(String id);

    /**
     * 插入文档
     * @param doc 文档信息
     * @return 文档信息
     */
    Doc insertDoc(Doc doc);

    /**
     * 删除文档
     * @param id 文档信息
     * @return 结果(文档id)
     */
    String deleteDoc(String id);

    /**
     * 关键字检索文档
     * @param searchValue 关键字
     * @return 结果集
     */
    Object searchDoc(String searchValue);
}
