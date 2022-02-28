# Yarda-ElasticSearch
### Elastic Stack

Elastic Stack 日志分析系统(Elastic Search的典型应用场景之一)，前身缩写是ELK，就是ElasticSearch + LogStash + Kibana + Beats，其中：

ElasticSearch：日志检索分析，提供搜集、分析、存储数据三大功能

Kibana：数据可视化平台

Logstash：日志收集系统，主要用来日志的搜集、分析、过滤，支持大量的数据获取方式。一般为c/s架构，client安装在需要收集日志的主机上，server端负责将收集到各节点日志过滤、修改等操作，再一并发送到elasticsearch

Beats：轻量级日志采集器。早期的ELK架构采用Logstash收集、解析日志，但是Logstash对内存、cpu、io等资源消耗比较高。相比Logstash，beats所占用系统CPU和内存几乎可以忽略

![image-20220221105243603](https://github.com/Cola-Ice/Yarda-ElasticSearch/raw/master/doc/image/image-20220221105243603.png)

#### ElasticSearch是什么？

ElasticSearch 是一个实时的分布式存储、搜索、分析的引擎，基于lucene开发的

#### 为什么要用ElasticSearch?

相较与数据库，ElasticSearch 是专门做搜索的：

ElasticSearch 对模糊搜索非常擅长（搜索速度很快）

从ElasticSearch搜索到的数据可以根据评分过滤，返回评分高的给用户（原生支持排序）

没那么准确的关键字也能搜索出相关结果（能匹配有相关性的记录）

#### ElasticSearch的数据结构

**倒排索引：**ElasticSearch采用倒排索引根据文章关键字建立索引，再通过关键查找对应的记录

ElasticSearch内置了一些**分词器**，主要由三部分组成：

- 文本过滤器，去除HTML（Character Filters）
- 按照规则切分，比如空格（Tokenizer）
- 将切分后的词进行处理，比如转成小写（TokenFilter）

**ElasticSearch的数据结构：**

![image-20220221114005642](https://github.com/Cola-Ice/Yarda-ElasticSearch/raw/master/doc/image/image-20220221114005642.png)

ElasticSearch会根据分词器对我们的文字进行分词，这些分词汇总起来我们叫做Term Dictionary，而我们需要通过分词找到对应的记录，这些文档ID保存在PostingList

**Term Dictionary：**ElasticSearch会根据分词器对我们的文字进行分词，这些分词汇总起来我们叫做Term Dictionary，由于其中的词非常多，ElasticSearch会对其进行排序，等要查询时就可以二分查找

**PostingList：**分词对应的文档ID保存在该列表

**Term Index：**由于Term Dictionary中的分词实在太多，不可能全部放在内存中，于是ElasticSearch又抽出了一层叫做Term Index，这层只存储词的前缀，Term Index会存储在内存中（检索特别快）。Term Index在内存中以**FST**（Finite State Transducers）形式保存，其特点：

- 空间占用小。通过词典中单词前缀和后缀的重复利用，压缩存储空间
- 查询速度快。O(len(str))的查询时间复杂度

#### ElasticSearch概念和架构

**基本概念：**

Index(索引)：ElasticSearch的index相当于数据库的table

Type(类型)：新版本的ElasticSearch已经废除(以前版本一个index下支持多个Type)

Document(文档)：Document相当于数据的一行记录

Field(属性)：相当于数据库的column

Mapping：相当于数据库的Schema的概念

DSL：相当于数据库的SQL

**ElasticSearch架构：**

1. 一个ElasticSearch集群会有多个ElasticSearch节点(运行着ElasticSearch进程的机器)

2. Master Node负责维护索引元数据、负责切换主分片和副分片身份等工作，如果主节点挂了，会选举产生新的master节点

3. ElasticSearch中一个Index的数据可以分发到不同的节点上存储，这个操作就叫做分片

   > 为什么要分片？
   >
   > 1. 如果一个Index的数据量太大，只有一个分片，那只会在一个节点上存储，随着数据量的增长，一个节点未必能把一个Index存储下来
   >
   > 2. 多个分片，在读写时可以并行操作（从多个节点读写数据，提高吞吐量）

4. 分片会有主分片和副分片之分(为了实现高可用)，数据写入的时候是写到主分片，副分片会复制主分片的数据，读取的时候主分片和副本分片都可以读

   > Index需要设置多少个主分片和副分片都是可以配置的

5. 当某个节点挂了，Master Node就会把对应的副本分片提拔为主分片，这样即便节点挂了，数据也不会丢失

#### ElasticSearch写入流程

客户端写入一条数据，到ElasticSearch集群就是由节点来处理这次请求，集群上的每个节点都是coordinating node(协调节点)，协调节点通过hash算法计算并路由到对应的处理节点

路由到对应的节点时，会执行一些流程：

1. 将数据写入到内存缓存区
2. 将数据写入到translog缓存区
3. 每个1s将内存缓存区的数据refresh到FileSystemCache中，生成segement文件，一旦生成segement文件，就可以通过索引查询到了
4. 每隔5s，translog缓存区commit到磁盘
5. 定期/定量从FileSystemCache中，结合translog内容flush index到磁盘

![image-20220221141456366](https://github.com/Cola-Ice/Yarda-ElasticSearch/raw/master/doc/image/image-20220221141456366.png)

> 补充说明：
>
> - ElasticSearch会把数据先写入到内存缓存区，然后每隔1s刷新到文件系统缓存区(生成segement文件，这个时候数据才可以被检索到)，所以：ElasticSearch写入的数据需要1s才能查询到
> - 为了防止宕机，内存中的数据跌势，ElasticSearch会另外写一份数据到日志文件，但这个最开始也是写道内存缓冲区的，每隔5s会将缓冲区的tanslog文件commit到磁盘，所以：ElasticSearch某个节点挂了，可能会造成5s的数据丢失
> - 等到磁盘上的tranlog文件达到一定程度或者超过30分钟，就会触发flush data操作，将内存中的segement文件(结合translog文件)异步刷到磁盘中，完成持久化操作

#### ElasticSearch更新和删除

1. 删除操作就打上delete状态，更新操作就把原来的doc标识为delete，然后重新插入一条

2. 前边提到，每隔1s会生成一个segement文件，那segement文件会越来越多，ElasticSearch会有一个merge任务，会将多个segement文件合并生成一个segement文件，合并过程中，会把带有delete状态的doc给物理删除

#### ElasticSearch查询

**最简单的方式分为两种：**

1.根据ID查询doc（实时的）

- 检索内存的Translog文件
- 检索硬盘的Translog文件
- 检索硬盘的Segement文件

2.根据query(搜索词)去查询匹配的doc（近实时的）

- 从内存和硬盘的segement文件查找

**查询可以分为三个阶段：**

QUERY_AND_FETCH：查询完就返回整个doc文档

QUERY_THEN_FETCH：先查询出对应的doc id，然后再去匹配对应的文档

DFS_QUERY_THEN_FETCH：先算分，再查询(分指的是**词频率和文档频率**)

常用的为QUERY_THEN_FETCH(查询然后读取)：

1. 向各个主分片和副本分片发送请求
2. 得到各个节点返回的doc id，组成doc id集合
3. 再次请求各个分片拿到相应的完整的doc

#### ElasticSearch+Kibana环境搭建

安装elasticsearch：docker run -d --name elasticsearch --net elastic -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms256m -Xmx256m" elasticsearch:7.17.0

安装kibana：docker run -d --name kibana --net elastic -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" kibana:7.17.0

#### ElasticSearch Java客户端

Java与ElasticSearch通信时有两种选择：

1.基于HTTP的Rest API

2.~~使用ElasticSearch本身用于节点到节点通信的内部Java API~~（7.*版本已弃用）

> 两者比较：
>
> 1.性能比较，Java API理论更高，实际测试非常接近：当用户向ElasticSearch节点发送Rest请求时，协调节点会解析JSON文本将其转换为相应的Java对象，并使用传输网络层以二进制格式发送到集群中其他节点。Java用户使用Transport Client直接在程序中构建这些Java对象，然后使用传输层传递相同二进制格式发出请求，从而跳过Rest所需的解析步骤
>
> 2.向后兼容性：Rest API与ElasticSearch的耦合性地，更稳定，可以与集群同步升级；使用Java API在升级ElasticSearch集群时，需要同步升级所有Java Transport Client
>
> 3.安全性：Rest API更好，可以通过HTTPS保护
>
> 4.依赖项：Rest API依赖更少，Java API依赖于整个ElasticSearch项目

ElasticSearch依赖(Spring Boot环境)：

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

相关文档：

https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#reference

https://www.zhihu.com/question/469207536/answer/2290001606

https://zhuanlan.zhihu.com/p/62892586

