package com.lemo.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemo.model.Employee;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 王兴岭
 * @create 2017-08-17 20:18
 */
@Slf4j
public class EmployeeTest extends BaseTest {

  private List<Employee> employees;
  private ObjectMapper objectMapper;


  public void pepare() {
    employees = new ArrayList<Employee>();

    Employee employee = new Employee();
    employee.setId(1);
    employee.setFirstName("John");
    employee.setLastName("Smith");
    employee.setAge(20);
    employee.setAbout("I love to go rock climbing");
    employee.setInterests(new String[]{"sports", "music"});
    employees.add(employee);

    Employee employee2 = new Employee();
    employee2.setId(2);
    employee2.setFirstName("Jane");
    employee2.setLastName("Smith");
    employee2.setAge(32);
    employee2.setAbout("I like to collect rock albums");
    employee2.setInterests(new String[]{"music"});
    employees.add(employee2);

    Employee employee3 = new Employee();
    employee3.setId(3);
    employee3.setFirstName("Douglas");
    employee3.setLastName("Fir");
    employee3.setAge(35);
    employee3.setAbout("I like to build cabinets");
    employee3.setInterests(new String[]{"forestry"});
    employees.add(employee3);

  }

  /**
   * 创建 employee 索引 （名词 等同于关系型数据库的 database） 创建索引的时候需要提供文档，在java中无法直接创建空的索引 index
   */
  @Test
  public void createIndex() throws JsonProcessingException {
    objectMapper = new ObjectMapper();
    pepare();
    for (Employee employee : employees) {
      byte[] bytes = objectMapper.writeValueAsBytes(employee);
      IndexResponse response = client.prepareIndex("megacorp", "employee").setId(employee.getId() + "").setSource(bytes, XContentType.JSON).setCreate(true).get();
      log.info("response:{}", response);
    }
  }

  /**
   * 根据id 检索
   */
  @Test
  public void searchByIdTest() {
    SearchResponse response = client.prepareSearch("megacorp").setQuery(QueryBuilders.idsQuery("employee").addIds("2")).get();
    log.info("response:{}", response);
  }

  /**
   * @desc 查询所有的员工信息
   * @author 王兴岭
   * @email wxlhdm@qq.com
   * @create 2017/8/17 21:10
   **/
  @Test
  public void searchAllTest() throws IOException {
    XContentBuilder megacorp = client.prepareSearch("megacorp")
            .setQuery(QueryBuilders.matchAllQuery()).get()
            .toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = megacorp.string();
    log.info("response:{}", string);
  }

  /**
   * @desc 根据姓进行检索， file:last_name
   * @author 王兴岭
   * @email wxlhdm@qq.com
   * @create 2017/8/17 21:26
   **/
  @Test
  public void searchByLastName() throws IOException {
    SearchResponse response = client.prepareSearch("megacorp").setQuery(QueryBuilders.matchQuery("last_name", "Smith")).get();
    XContentBuilder content = response.toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = content.string();
    log.info("response:{}", string);
  }

  /**
   * 使用 BoolQueryBuilder组合查询
   */
  @Test
  public void dslSearchTest() throws IOException {
    MatchQueryBuilder builder = QueryBuilders.matchQuery("last_name", "Smith");
    RangeQueryBuilder filter = QueryBuilders.rangeQuery("age").gte(30);
    BoolQueryBuilder must = QueryBuilders.boolQuery().must(builder).must(filter);
    SearchResponse response = client.prepareSearch("megacorp").setQuery(must).get();
    XContentBuilder content = response.toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = content.string();
    log.info("response:{}", string);
  }

  /**
   * Elasticsearch 默认按照相关性得分排序，即每个文档跟查询的匹配程度。第一个最高得分的结果很明显：John Smith 的 about 属性清楚地写着 “rock
   * climbing” 。
   *
   * 但为什么 Jane Smith 也作为结果返回了呢？原因是她的 about 属性里提到了 “rock” 。因为只有 “rock” 而没有 “climbing” ， 所以她的相关性得分低于
   * John 的。
   *
   * 这是一个很好的案例，阐明了 Elasticsearch 如何 在 全文属性上搜索并返回相关性最强的结果。 Elasticsearch中的 相关性
   * 概念非常重要，也是完全区别于传统关系型数据库的一个概念， 数据库中的一条记录要么匹配要么不匹配。
   *
   * @desc 全文检索
   * @author 王兴岭
   * @email wxlhdm@qq.com
   * @create 2017/8/17 21:51
   **/
  @Test
  public void textSearchTest() throws IOException {
    MatchQueryBuilder about = QueryBuilders.matchQuery("about", "rock climbing");
    SearchResponse response = client.prepareSearch("megacorp").setQuery(about).get();
    XContentBuilder content = response.toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = content.string();
    log.info("response:{}", string);
  }

  /**
   * 找出一个属性中的独立单词是没有问题的，但有时候想要精确匹配一系列单词或者短语 。
   * 比如， 我们想执行这样一个查询，仅匹配同时包含 “rock” 和 “climbing” ，
   * 并且 二者以短语 “rock climbing” 的形式紧挨着的雇员记录
   * <a href="https://www.elastic.co/guide/cn/elasticsearch/guide/current/_phrase_search.html">https://www.elastic.co/guide/cn/elasticsearch/guide/current/_phrase_search.html</a>
   * @desc 短语搜索
   * @author 王兴岭
   * @email wxlhdm@qq.com
   * @create 2017/8/17 21:59
   */
  @Test
  public void phraseSearchTest() throws IOException {
    MatchPhraseQueryBuilder about = QueryBuilders.matchPhraseQuery("about", "rock climbing");
    SearchResponse response = client.prepareSearch("megacorp").setQuery(about).get();
    XContentBuilder content = response.toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = content.string();
    log.info("response:{}", string);
  }

  /**
   *
   * @desc 高亮搜索
   * response:
   * {
  "took" : 535,
  "timed_out" : false,
  "_shards" : {
  "total" : 5,
  "successful" : 5,
  "failed" : 0
  },
  "hits" : {
  "total" : 1,
  "max_score" : 0.53484553,
  "hits" : [
  {
  "_index" : "megacorp",
  "_type" : "employee",
  "_id" : "1",
  "_score" : 0.53484553,
  "_source" : {
  "id" : 1,
  "age" : 20,
  "about" : "I love to go rock climbing",
  "interests" : [
  "sports",
  "music"
  ],
  "first_name" : "John",
  "last_name" : "Smith"
  },
  "highlight" : {
  "about" : [
  "I love to go <em>rock</em> <em>climbing</em>"
  ]
  }
  }
  ]
  }
  }
   * @author 王兴岭
   * @email wxlhdm@qq.com
   * @create 2017/8/17 22:04
  **/
  @Test
  public void highlightSearchTest() throws IOException{
    MatchPhraseQueryBuilder about = QueryBuilders.matchPhraseQuery("about", "rock climbing");

    HighlightBuilder highlightBuilder = new HighlightBuilder().field("about");
    SearchResponse response = client.prepareSearch("megacorp").setQuery(about).highlighter(highlightBuilder).get();
    XContentBuilder content = response.toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = content.string();
    log.info("response:{}", string);
  }

  /**
   * @desc 聚合 aggregations
   * @author 王兴岭
   * @email wxlhdm@qq.com
   * @create 2017/8/17 22:12
  **/
  @Test
  public void aggregationsTest() throws IOException {
    ValueCountAggregationBuilder field = AggregationBuilders.count("all_interests").field("interests");
    SearchResponse response = client.prepareSearch("megacorp").setQuery(QueryBuilders.matchAllQuery()).addAggregation(field).get();
    XContentBuilder content = response.toXContent(JsonXContent.contentBuilder().prettyPrint(), ToXContent.EMPTY_PARAMS);
    String string = content.string();
    log.info("response:{}", string);
  }
}
