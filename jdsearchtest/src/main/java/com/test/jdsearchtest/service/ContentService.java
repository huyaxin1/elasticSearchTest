package com.test.jdsearchtest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.jdsearchtest.pojo.Content;
import com.test.jdsearchtest.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContentService {

    @Resource(name = "restHighLevelClient")
    private RestHighLevelClient client;

    public boolean createContent(String keyword){
        List<Content> contents = HtmlParseUtil.parseHtml(keyword);
        ObjectMapper mapper = new ObjectMapper();
        BulkRequest request=new BulkRequest("jd_goods");
        request.timeout("60s");
        for (Content content : contents){
            try {
                request.add(new IndexRequest().source(mapper.writeValueAsString(content),XContentType.JSON));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        BulkResponse response = null;
        try {
            response = client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.hasFailures();
    }

    public List<Map<String,Object>> searchPage(String keyword,int pageNum,int pageSize){
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //查询条件
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("name", keyword);
        sourceBuilder.query(queryBuilder);
        //分页设置
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse=null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            list.add(hit.getSourceAsMap());
        }
        return list;
    }

    public List<Map<String,Object>> highlightShow(String keyword,int pageNum,int pageSize){
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");//设置高亮的字段
        highlightBuilder.requireFieldMatch(true);//是否将所有搜索的关键字进行高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //查询条件
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("name", keyword);
        sourceBuilder.query(queryBuilder);
        //分页设置
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse=null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
            HighlightField highlightField = hit.getHighlightFields().get("name");
            Text[] fragments = highlightField.fragments();
            for (Text fragment : fragments) {
                //fragment.toString()不要直接用fragment
                sourceAsMap.put("name",fragment.toString());
            }
            list.add(sourceAsMap);
        }
        System.out.println(list);
        return list;
    }
}
