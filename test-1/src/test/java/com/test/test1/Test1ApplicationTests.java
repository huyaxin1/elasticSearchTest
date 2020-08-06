package com.test.test1;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.test.test1.pojo.User;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@SpringBootTest
class Test1ApplicationTests {

    @Resource(name = "restHighLevelClient")
    private RestHighLevelClient client;

    @Test
//创建索引
    void test1() throws IOException {
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("first_index");
        //客户端执行请求 IndecesClient，请求后获得响应
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
//获取索引
    void test2() throws IOException {
        GetIndexRequest request = new GetIndexRequest("second_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
//删除索引
    void test3() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("first_index");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
//索引中添加数据
    void test4() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        User user = new User("张三", 17);

        IndexRequest request = new IndexRequest("school_index");
        //规则 put school_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");
        //将数据转成json串的格式进行请求
        request.source(mapper.writeValueAsString(user), XContentType.JSON);
        //发送请求，获取响应结果
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

    @Test
//指定的索引及id是否存在
    void test5() throws IOException {
        GetRequest request = new GetRequest("school_index", "1");
        //不返回 _source上下文数据
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
    void test6() throws IOException {
        GetRequest request = new GetRequest("school_index", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println(response);
        //获取文档数据 还可以得到一个map对象
        System.out.println(response.getSourceAsString());
    }

    @Test
    void test7() throws IOException {
        UpdateRequest request = new UpdateRequest("school_index", "1");
        request.timeout("1s");
        ObjectMapper mapper = new ObjectMapper();
        User user = new User("张三", 18);
        request.doc(mapper.writeValueAsString(user), XContentType.JSON);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
//删除数据
    void test8() throws IOException {
        DeleteRequest request = new DeleteRequest("school_index", "1");
        request.timeout("1s");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    @Test
//批量添加数据
    void test9() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("张三", 17));
        userList.add(new User("李四", 17));
        userList.add(new User("王五", 18));
        userList.add(new User("赵六", 17));
        userList.add(new User("阿七", 18));
        userList.add(new User("小八", 17));
        ObjectMapper mapper = new ObjectMapper();
        //批量处理请求
        for (int i = 0; i < userList.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("school_index")
                            .id(i + 1 + "")
                            .source(mapper.writeValueAsString(userList.get(i)), XContentType.JSON));

        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否添加成功，返回false表示成功
        System.out.println(bulkResponse.hasFailures());
    }

    @Test
    //查询
    void test10() throws IOException {
        //可以指定多个索引
        SearchRequest searchRequest = new SearchRequest("school_index");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("name");
        searchSourceBuilder.highlighter(highlightBuilder);
        //查询条件 使用 QueryBuilders构建
        // QueryBuilders.termQuery() 精准查询
        // QueryBuilders.matchQuery() 匹配查询
        // QueryBuilders.matchAllQuery() 查询全部
        //TermQueryBuilder queryBuilder = QueryBuilders.termQuery("name", "李");
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("name", "李四");
        //MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        //分页
        //searchSourceBuilder.from(0);
        //searchSourceBuilder.size(1);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse);
        System.out.println("======================");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            //获取以 map形式的数据
            System.out.println(hit.getSourceAsMap());
        }
    }

    @Test
    void test11() throws IOException {
        SearchRequest searchRequest = new SearchRequest("school_index");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("name");
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "李四");
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(matchQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
            if(name!=null){
                Text[] fragments = name.fragments();
                for (Text fragment : fragments) {
                    System.out.println(fragment);
                }
            }
        }

    }

}
