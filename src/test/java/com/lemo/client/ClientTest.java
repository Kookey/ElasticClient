package com.lemo.client;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 王兴岭
 * @create 2017-08-17 11:20
 */
@Slf4j
public class ClientTest extends BaseTest {



  /**
   * 测试连接
   */
  @Test
  public void connectTest(){
    List<DiscoveryNode> nodes = client.connectedNodes();
    System.out.println(nodes.size());
    for (DiscoveryNode node: nodes) {
      System.out.println(node.getName());
    }
  }

  /**
   * 插入
   */
  @Test
  public void insertTest(){
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("id",2);
    map.put("name","houdm");
    map.put("age",30);
    IndexResponse response = client.prepareIndex("user", "put","2").setSource(map).setOpType(DocWriteRequest.OpType.CREATE).get();
    RestStatus status = response.status();
    log.info("status:{}",status);
  }

  /**
   * 绝对匹配查询
   */
  @Test
  public void searchTest(){
    SimpleQueryStringBuilder builder = new SimpleQueryStringBuilder("20").field("age");

    SearchResponse user = client.prepareSearch("user").setQuery(builder).get();
    log.info("user:{}",user);

  }

  /**
   * 模糊匹配 以指定字符为前缀
   */
  @Test
  public void  searchLike(){
    SearchResponse response = client.prepareSearch("user").setQuery(new PrefixQueryBuilder("name", "w")).get();
    log.info("user:{}",response);
  }

  /**
   * 通配符匹配
   */
  @Test
  public void searchWild(){
    SearchResponse response = client.prepareSearch("user").setQuery(new WildcardQueryBuilder("name", "w*")).get();
    log.info("user:{}",response);
  }

  /**
   *范围匹配查询， 比如我们插入的人的年龄是20
   * 那我们条件可以是大于10
   * or <30
   * or 10<x<30
   * 等类似这种的查询
   */
  @Test
  public void rangeSearchTest(){
    //大于10
    QueryBuilder builder = new RangeQueryBuilder("age").gte(10);
    SearchResponse user = client.prepareSearch("user").setQuery(builder).get();
    log.info("user:{}",user);
  }

  /**
   * 正则 ，RegexpQueryBuilder
   * 正则表达式地址：<a href="http://jquery.cuishifeng.cn/regexp.html">http://jquery.cuishifeng.cn/regexp.html</a>
   */
  @Test
  public void RegexpSearchTest(){
    QueryBuilder builder = new RegexpQueryBuilder("name","h.*");
    SearchResponse user = client.prepareSearch("user").setQuery(builder).get();
    log.info("user:{}",user);
  }


  /**
   * 多查询，elasticsearch 会根据查询的条件返回多查询的结果
   */
  @Test
  public void MultiSearchTest(){
    SearchRequestBuilder srb1 = client
            .prepareSearch().setQuery(QueryBuilders.queryStringQuery("wangxl")).setSize(1);
    SearchRequestBuilder srb2 = client
            .prepareSearch().setQuery(QueryBuilders.matchQuery("age", 20)).setSize(1);

    MultiSearchResponse sr = client.prepareMultiSearch()
            .add(srb1)
            .add(srb2)
            .get();
    log.info("sr:{}",sr);
  }

}
