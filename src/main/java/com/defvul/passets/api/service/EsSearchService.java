package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryUrlForm;
import com.defvul.passets.api.bo.res.IpPortBO;
import com.defvul.passets.api.bo.res.UrlBO;
import com.defvul.passets.api.bo.res.UrlInfoBO;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 说明:
 * 时间: 2019/11/8 15:03
 *
 * @author wimas
 */
@Slf4j
@Service
public class EsSearchService {
    @Autowired
    private RestHighLevelClient client;

    @Value("${elasticsearch.index}")
    private String index;

    private static final int SIZE = 2147483647;

    /**
     * 查询一段时间内的IP和端口
     *
     * @param form
     * @return
     */
    public List<IpPortBO> queryTimeSlotWithIpAndPort(QueryBaseForm form) {
        String termName = "ip_port";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.query(getBoolQueryWithQueryForm(form));
        sourceBuilder.aggregation(AggregationBuilders.terms(termName).field("src.keyword").size(SIZE));

        request.source(sourceBuilder);

        SearchResponse response = search(request);
        if (response == null) {
            return Collections.emptyList();
        }

        Terms terms = response.getAggregations().get(termName);
        List<IpPortBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            String key = bucket.getKeyAsString();
            String[] values = key.split(":");
            result.add(new IpPortBO(values[0], values[1]));
        }
        return result;
    }

    public List<UrlBO> queryTimeSlotWithUrl(QueryBaseForm form) {
        String termName = "urls";
        String childTermName = "urls_child";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.query(getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder aggregation = AggregationBuilders.terms(termName).field("url.host.keyword");
        aggregation.size(SIZE);

        TermsAggregationBuilder urlsChild = AggregationBuilders.terms(childTermName).field("url.url.keyword");
        urlsChild.size(SIZE);
        aggregation.subAggregation(urlsChild);

        sourceBuilder.aggregation(aggregation);

        request.source(sourceBuilder);
        SearchResponse response = search(request);
        if (response == null) {
            return Collections.emptyList();
        }

        Terms terms = response.getAggregations().get(termName);
        List<UrlBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            String key = bucket.getKeyAsString();
            Terms termNode = bucket.getAggregations().get(childTermName);
            List<String> urls = new ArrayList<>();
            for (Terms.Bucket urlBucket : termNode.getBuckets()) {
                urls.add(urlBucket.getKeyAsString());
            }
            result.add(new UrlBO(key, urls));
        }
        return result;
    }

    private BoolQueryBuilder getBoolQueryWithQueryForm(QueryBaseForm form) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp");
        if (form.getStart() != null) {
            rangeQueryBuilder.gte(form.getStart());
        }

        if (form.getEnd() != null) {
            rangeQueryBuilder.lte(form.getEnd());
        }

        boolQueryBuilder.must(rangeQueryBuilder);

        // 是否只查询内网地址
        if (form.isOnlyInner()) {
            TermQueryBuilder innerQueryBuilder = QueryBuilders.termQuery("src_inner", true);
            boolQueryBuilder.must(innerQueryBuilder);
        }

        return boolQueryBuilder;
    }

    public List<UrlInfoBO> queryByUrl(QueryUrlForm form) {
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.size(10000);
        BoolQueryBuilder boolQueryBuilder = getBoolQueryWithQueryForm(form);
        boolQueryBuilder.must(QueryBuilders.termQuery("url.url.keyword", form.getUrl()));
        sourceBuilder.query(boolQueryBuilder);

        request.source(sourceBuilder);
        SearchResponse response = search(request);
        if (response == null) {
            return Collections.emptyList();
        }

        Iterator<SearchHit> iterator = response.getHits().iterator();
        List<UrlInfoBO> result = new ArrayList<>();

        while (iterator.hasNext()) {
            SearchHit hit = iterator.next();
            result.add(new Gson().fromJson(hit.getSourceAsString(), UrlInfoBO.class));
        }

        return result;
    }

    private SearchResponse search(SearchRequest request) {
        try {
            return client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private SearchRequest getSearchRequest() {
        return new SearchRequest(index);
    }

    private SearchSourceBuilder getSourceBuilder() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        return sourceBuilder;
    }
}
