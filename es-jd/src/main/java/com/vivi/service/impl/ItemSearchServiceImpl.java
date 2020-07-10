package com.vivi.service.impl;

import com.alibaba.fastjson.JSON;
import com.vivi.service.ItemSearchService;
import com.vivi.vo.ItemVO;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wangwei
 * 2020/7/10 7:01
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 创建索引
     *
     * 本来应该先检查索引是否存在，存在则先删除
     * @param indexName
     * @return
     */
    @Override
    public boolean createItemIndex(String indexName) {
        // 判断索引是否存在，存在就先删除
        if (isIndexExist(indexName)) deleteIndex(indexName);
        // 创建索引请求
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
        // 可选参数，备份2，碎片3
        indexRequest.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
        // 可选参数，超时时间
        indexRequest.setTimeout(TimeValue.timeValueSeconds(2));
        // 字段和类型，可选
        indexRequest.mapping(
                "{\n" +
                        "  \"properties\": {\n" +
                        "    \"title\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"price\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"imgUrl\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"publisher\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                XContentType.JSON);
        try {
            // 同步方式
            client.indices().create(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("---创建ES索引失败---");
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public void bulkSave(String indexName, List<ItemVO> itemVOList) {

        // 批量插入请求
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        // 写入完成立即刷新，否则会有延时，前端获取不到数据
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        itemVOList.forEach(itemVO -> {
            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.source(JSON.toJSONString(itemVO), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("---批量插入ES失败---");
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, Object>> highLightSearch(String indexName, String field, String keyword, Integer pageNum, Integer pageSize) {
        List<Map<String, Object>> resList = new ArrayList<>();

        /**
         * 1. 创建 SearchRequest
         * Creates the SearchRequest. Without arguments this runs against all indices.
         *
         * 2. 创建 SearchSourceBuilder
         * Most search parameters are added to the SearchSourceBuilder. It offers setters for everything that goes into the search request body.
         *
         * 3. 在SearchSourceBuilder中设置搜索规则
         * Add a match_all query to the SearchSourceBuilder.
         *
         * 4. 将 SearchSourceBuilder 添加到 SearchRequest
         * Add the SearchSourceBuilder to the SearchRequest.
         *
         * 5. 执行搜索
         */
        // 1. 创建 SearchRequest
        SearchRequest searchRequest = new SearchRequest(indexName);

        // 2. 创建 SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 3. 在SearchSourceBuilder中设置搜索规则，
        // 使用term精确匹配
        // searchSourceBuilder.query(QueryBuilders.termQuery(field, keyword));
        // 使用term精确匹配时，中文查询失败，因此改用match匹配
        searchSourceBuilder.query(QueryBuilders.matchQuery(field, keyword));
        // 设置分页参数
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 设置高亮规则
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(field);
        highlightBuilder.preTags("<em><font class=\"skcolor_ljg\">");
        highlightBuilder.postTags("</font></em>");
        searchSourceBuilder.highlighter(highlightBuilder);

        // 4. 将 SearchSourceBuilder 添加到 SearchRequest
        searchRequest.source(searchSourceBuilder);
        try {
            // 5. 搜索
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            // 6. 查询到的文档
            SearchHit[] hits = response.getHits().getHits();
            for (SearchHit hit : hits) {
                // 获取到全部字段高亮部分
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                // 获取 指定field部分高亮出的片段
                Text[] fragments = highlightFields.get(field).fragments();
                // 拼装
                StringBuilder builder = new StringBuilder();
                for (Text text : fragments) {
                    builder.append(text);
                }
                // 用高亮后的结果取代原来字段
                Map<String, Object> source = hit.getSourceAsMap();
                source.put(field, builder.toString());
                // 将这条记录加入结果集
                resList.add(source);
            }
        } catch (IOException e) {
            System.out.println("===执行搜索失败===");
            e.printStackTrace();
        }
        return resList;
    }

    private boolean isIndexExist(String index) {
        GetIndexRequest request = new GetIndexRequest(index);
        try {
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            if (exists) return true;
        } catch (IOException e) {
            System.out.println("===判断索引是否存在请求失败");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除索引
     * @param indexName
     */
    private void deleteIndex(String indexName) {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        request.timeout("2m");
        try {
            client.indices().delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("===删除索引失败===");
            e.printStackTrace();
        }
    }

}
