package com.yarda.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.util.Date;

/**
 * 文档模型
 * @author xuezheng
 * @version 1.0
 * @date 2022/2/23 16:57
 */
@Document(indexName = "doc")
@Data
public class Doc implements Serializable {
    /** 文档记录id */
    @Id
    private String id;
    /** 文档标题 */
    @Field("title")
    private String title;
    /** 文档内容 */
    @Field("content")
    private String content;
    /** 文档记录时间 */
    @Field("date")
    private Date date;
}
