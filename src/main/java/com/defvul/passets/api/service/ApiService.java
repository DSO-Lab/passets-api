package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.res.BaseInfoBO;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 说明:
 * 时间: 2020/3/25 13:56
 *
 * @author wimas
 */
@Slf4j
@Service
public class ApiService {
    @Autowired
    private EsSearchService esSearchService;

    private static final String[] INCLUDE_HOST = new String[]{
            "ip",
            "port",
            "inner",
            "host",
            "path",
            "site",
            "apps",
            "title",
            "url",
            "url_tpl",
            "code",
            "@timestamp",
    };
    private static final String[] INCLUDE_SITE = new String[]{
            "ip",
            "port",
            "inner",
            "host",
            "path",
            "site",
            "apps",
            "title",
            "url",
            "url_tpl",
            "code",
            "@timestamp",
    };

    public List<BaseInfoBO> queryHosts(QueryBaseForm form) {
        String hostTermName = "host_keyword";
        String hitTermName = "score_hits";
        SearchSourceBuilder builder = esSearchService.getSourceBuilder();
        builder.query(esSearchService.getBoolQueryWithQueryForm(form));

        // 主机聚合
        TermsAggregationBuilder hostTermsBuilder = AggregationBuilders.terms(hostTermName).field("host.keyword").size(EsSearchService.SIZE);
        // 获取数据源， 根据title排序
        List<SortBuilder<?>> sorts = new ArrayList<>();
        sorts.add(SortBuilders.fieldSort("pro.keyword").order(SortOrder.ASC));
        sorts.add(SortBuilders.fieldSort("title").unmappedType("keyword").order(SortOrder.DESC));
        TopHitsAggregationBuilder hitsAggregationBuilder = AggregationBuilders.topHits(hitTermName).size(1).sorts(sorts);
        hitsAggregationBuilder.fetchSource(INCLUDE_HOST, null);
        hostTermsBuilder.subAggregation(hitsAggregationBuilder);

        builder.aggregation(hostTermsBuilder);
        log.info("api_host_query: {}", builder);
        SearchResponse response = esSearchService.search(builder);
        if (response == null || response.getAggregations() == null) {
            return Collections.emptyList();
        }

        Terms terms = response.getAggregations().get(hostTermName);
        List<BaseInfoBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            ParsedTopHits hits = bucket.getAggregations().get(hitTermName);
            BaseInfoBO bo = new Gson().fromJson(hits.getHits().getAt(0).getSourceAsString(), BaseInfoBO.class);
            bo.setCount(bucket.getDocCount());
            result.add(bo);
        }
        return result;
    }

    public List<BaseInfoBO> queryUrls(QueryInfoForm form) {
        if (StringUtils.isBlank(form.getValue())) {
            return null;
        }
        QueryBaseForm baseForm = form.toBaseForm(false);
        SearchSourceBuilder builder = esSearchService.getSourceBuilder();
        builder.query(esSearchService.getBoolQueryWithQueryForm(baseForm));

        String urlTplTermName = "site_url_tpl";
        String topHits = "site_top_hits";
        // url_tpl聚合
        TermsAggregationBuilder urlsChildAgg = AggregationBuilders.terms(urlTplTermName).field("url_tpl.keyword").size(EsSearchService.SIZE);
        // 源数据
        TopHitsAggregationBuilder topHitsAgg = AggregationBuilders.topHits(topHits)
                .fetchSource(INCLUDE_SITE, null).sort("@timestamp", SortOrder.DESC).size(1);
        urlsChildAgg.subAggregation(topHitsAgg);
        builder.aggregation(urlsChildAgg);

        log.info("api_site_query: {}", builder);
        SearchResponse response = esSearchService.search(builder);
        if (response == null || response.getAggregations() == null) {
            return null;
        }

        List<BaseInfoBO> result = new ArrayList<>();
        Terms terms = response.getAggregations().get(urlTplTermName);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            ParsedTopHits parsedTopHits = bucket.getAggregations().get(topHits);
            BaseInfoBO bo = new Gson().fromJson(parsedTopHits.getHits().getAt(0).getSourceAsString(), BaseInfoBO.class);
            result.add(bo);
        }
        return result;
    }
}
