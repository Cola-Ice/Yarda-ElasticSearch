package com.yarda.elasticsearch.service.impl;

import com.yarda.elasticsearch.domain.Doc;
import com.yarda.elasticsearch.service.TestService;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * elasticsearch测试 service 实现
 * @author xuezheng
 * @version 1.0
 * @date 2022/2/23 17:26
 */
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private ElasticsearchRestTemplate template;

    /**
     * 获取文档详情--根据id
     * @param id 要查询的文档id
     * @return 文档详情
     */
    @Override
    public Doc getDocById(String id) {
        return template.get(id, Doc.class);
    }

    /**
     * 插入文档
     * @param doc 文档信息
     * @return 文档信息
     */
    @Override
    public Doc insertDoc(Doc doc) {
        doc.setDate(new Date());
        return template.save(doc);
    }

    /**
     * 删除文档
     * @param id 文档信息
     * @return 结果
     */
    @Override
    public String deleteDoc(String id) {
        return template.delete(id, Doc.class);
    }

    /**
     * 关键字检索文档
     * @param searchValue 关键字
     * @return 结果集
     */
    @Override
    public Object searchDoc(String searchValue) {
        Criteria miller = new Criteria("title").contains(searchValue);
        Query query = new CriteriaQuery(miller);
        return template.search(query, Doc.class);
    }
}
