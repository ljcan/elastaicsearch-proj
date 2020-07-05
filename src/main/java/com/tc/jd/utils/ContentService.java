package com.tc.jd.utils;

import com.alibaba.fastjson.JSON;
import com.tc.jd.pojo.Content;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
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
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {
    @Resource
    public RestHighLevelClient restHighLevelClient;

    /**
     * 爬取网站数据写入es
     * @param keyword
     * @param page
     * @return
     * @throws IOException
     */
    public Boolean parseContent(String keyword,String page) throws IOException {
        List<Content> contentList = HtmlParseUtil.parseHtmlByKey(keyword,page);
        //数据写入es
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("1m");
        for (int i = 0; i < contentList.size(); i++) {
            bulkRequest.add(new IndexRequest("j_goods")
                    .source(JSON.toJSONString(contentList.get(i)), XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    /**
     * 从es中搜索
     * @param keyword
     * @param pageNo
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> search(String keyword, Integer pageNo, Integer pageSize) throws IOException {
        SearchRequest request = new SearchRequest("j_goods");
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //分页查询
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //精确查询
        TermQueryBuilder query = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(query);
        //执行搜索
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit ele:response.getHits().getHits()){
            Map<String, Object> sourceAsMap = ele.getSourceAsMap();
            list.add(sourceAsMap);
        }
        return list;
    }

    /**
     * 从es中搜索，并且设置高亮搜索词
     * @param keyword
     * @param pageNo
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> searchHigh(String keyword, Integer pageNo, Integer pageSize) throws IOException {
        SearchRequest request = new SearchRequest("j_goods");
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //分页查询
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");        //设置高亮字段
        highlightBuilder.requireFieldMatch(false);      //多个高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);


        //精确查询
        TermQueryBuilder query = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(query);
        //执行搜索
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit ele:response.getHits().getHits()){

            Map<String, HighlightField> highlightFields = ele.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = ele.getSourceAsMap();   //没有高亮的搜索结果
            if(title!=null){
                Text[] fragments = title.fragments();
                String n_title="";
                for(Text text:fragments){
                    n_title+=text;
                }
                //将原来的字段替换为高亮的字段
                sourceAsMap.put("title",n_title);
            }
            list.add(sourceAsMap);
        }
        return list;
    }

}
