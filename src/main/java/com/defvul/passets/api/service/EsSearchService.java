package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.res.InfoBO;
import com.defvul.passets.api.bo.res.UrlBO;
import com.defvul.passets.api.vo.Page;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static final String[] INCLUDE_SOURCE = new String[]{
            "code",
            "@timestamp",
            "pro",
            "type",
            "port",
            "host",
            "url",
            "url_tpl",
            "header",
            "site",
            "apps",
            "inner",
            "path",
            "title",
            "ip",
    };

    private static final String[] INCLUDE_SOURCE_ALL = new String[]{
            "code",
            "@timestamp",
            "pro",
            "type",
            "port",
            "host",
            "url",
            "url_tpl",
            "server",
            "header",
            "site",
            "apps",
            "inner",
            "path",
            "body",
            "title",
            "ip",
    };

    private static final String[] INCLUDE_SOURCE_IP = new String[]{
            "@timestamp",
            "pro",
            "port",
            "host",
            "url_tpl",
            "site",
            "apps",
            "inner",
            "ip",
    };

    private static final int SIZE = 2147483647;

    @PostConstruct
    public void init() {
        ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest();
        request.persistentSettings(new HashMap<String, Object>(1) {{
            put("search.max_buckets", SIZE);
        }});
        try {
            client.cluster().putSettings(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("执行es设置报错: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 查询一段时间内的IP和端口
     *
     * @param form
     * @return
     */
    public List<InfoBO> queryTimeSlotWithIpAndPort(QueryBaseForm form) {
        String termName = "ip_port";
        String topName = "top_score_hits";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.query(getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(termName).field("host.keyword").size(SIZE);

        TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(topName).size(1).sort("@timestamp", SortOrder.DESC);
        topHitsAggregationBuilder.fetchSource(INCLUDE_SOURCE_IP, null);
        termsAggregationBuilder.subAggregation(topHitsAggregationBuilder);

        sourceBuilder.aggregation(termsAggregationBuilder);

        request.source(sourceBuilder);

        log.info("IP开始搜索");
        SearchResponse response = search(request);
        log.info("IP结束搜索");
        if (response == null) {
            return Collections.emptyList();
        }

        Terms terms = response.getAggregations().get(termName);
        List<InfoBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            ParsedTopHits hits = bucket.getAggregations().get(topName);
            String json = hits.getHits().getAt(0).getSourceAsString();
            InfoBO bo = new Gson().fromJson(json, InfoBO.class);
            bo.setCount(bucket.getDocCount());
            result.add(bo);
        }
        return result;
    }

    public Page<InfoBO> ipPage(QueryBaseForm form) {
        String termName = "ip_port";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.from(form.getCurrentPage() > 0 ? form.getCurrentPage() - 1 : form.getCurrentPage())
                .size(form.getPageSize())
                .sort("@timestamp", SortOrder.DESC);
        sourceBuilder.fetchSource(INCLUDE_SOURCE_IP, null);
        sourceBuilder.query(QueryBuilders.termQuery("state", 1));
        sourceBuilder.query(getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(termName).field("host.keyword").size(SIZE);
        termsAggregationBuilder.order(BucketOrder.aggregation("timestamp_order", false));

        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("timestamp_order").field("@timestamp");
        termsAggregationBuilder.subAggregation(maxAggregationBuilder);

        sourceBuilder.aggregation(termsAggregationBuilder);

        request.source(sourceBuilder);

        SearchResponse response = search(request);
        if (response == null) {
            return new Page<>();
        }

        Terms terms = response.getAggregations().get(termName);
        int total = terms.getBuckets().size();

        Page<InfoBO> page = new Page<>();
        List<InfoBO> result = new ArrayList<>();
        page.setCurrentPage(form.getCurrentPage());
        page.setPageSize(form.getPageSize());
        page.setTotal(total);

        SearchHits searchHits = response.getHits();
        for (SearchHit hit : searchHits.getHits()) {
            String json = hit.getSourceAsString();
            InfoBO bo = new Gson().fromJson(json, InfoBO.class);
            terms.getBuckets().parallelStream().filter(b -> b.getKey().equals(bo.getHost())).forEach(b -> bo.setCount(b.getDocCount()));
            result.add(bo);
        }
        if (result.size() > 0) {
            page.setData(result);
        }
        return page;
    }

    public List<UrlBO> queryTimeSlotWithUrl(QueryBaseForm form) {
        String termName = "urls";
        String childTermName = "urls_child";
        String topName = "top_score_hits";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.query(getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder aggregation = AggregationBuilders.terms(termName).field("site.keyword");
        aggregation.size(SIZE);

        TermsAggregationBuilder urlsChild = AggregationBuilders.terms(childTermName).field("url_tpl.keyword");
        urlsChild.size(SIZE);

        TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(topName).size(1).sort("@timestamp", SortOrder.DESC);
        topHitsAggregationBuilder.fetchSource(form.isFullField() ? INCLUDE_SOURCE_ALL : INCLUDE_SOURCE, null);
        urlsChild.subAggregation(topHitsAggregationBuilder);

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
            long count = bucket.getDocCount();
            Terms termNode = bucket.getAggregations().get(childTermName);
            List<InfoBO> urls = new ArrayList<>();
            for (Terms.Bucket urlBucket : termNode.getBuckets()) {
                ParsedTopHits hits = urlBucket.getAggregations().get(topName);
                String json = hits.getHits().getAt(0).getSourceAsString();
                InfoBO bo = new Gson().fromJson(json, InfoBO.class);
                bo.setCount(urlBucket.getDocCount());
                urls.add(bo);
            }
            result.add(new UrlBO(key, count, urls));
        }
        return result;
    }

    public List<UrlBO> urlAll(QueryBaseForm form) {
        String termName = "urls";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.query(getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder aggregation = AggregationBuilders.terms(termName).field("site.keyword");
        aggregation.size(SIZE).order(BucketOrder.aggregation("timestamp_order", false));

        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("timestamp_order").field("@timestamp");

        aggregation.subAggregation(maxAggregationBuilder);
        sourceBuilder.aggregation(aggregation);

        request.source(sourceBuilder);
        SearchResponse response = search(request);
        if (response == null) {
            return new ArrayList<>();
        }

        Terms terms = response.getAggregations().get(termName);

        List<UrlBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            result.add(new UrlBO(bucket.getKeyAsString(), bucket.getDocCount(), null));
        }
        return result.parallelStream().sorted(Comparator.comparing(UrlBO::getCount).reversed()).collect(Collectors.toList());
    }

    public Page<InfoBO> urlsPage(QueryBaseForm form) {
        if (form.getUrl() == null) {
            return new Page<>();
        }
        String termName = "urls";
        String childTermName = "urls_child";
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        sourceBuilder.from(form.getCurrentPage() > 0 ? form.getCurrentPage() - 1 : form.getCurrentPage())
                .size(form.getPageSize())
                .fetchSource(INCLUDE_SOURCE, null)
                .sort("@timestamp", SortOrder.DESC);
        BoolQueryBuilder boolQueryBuilder = getBoolQueryWithQueryForm(form);
        boolQueryBuilder.must(QueryBuilders.existsQuery("site"));
        sourceBuilder.query(boolQueryBuilder);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms(termName).field("site.keyword");
        aggregation.size(SIZE).order(BucketOrder.aggregation("timestamp_order", false));

        TermsAggregationBuilder urlsChild = AggregationBuilders.terms(childTermName).field("url_tpl.keyword");
        urlsChild.size(SIZE).order(BucketOrder.aggregation("timestamp_order", false));

        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("timestamp_order").field("@timestamp");
        urlsChild.subAggregation(maxAggregationBuilder);
        aggregation.subAggregation(maxAggregationBuilder).subAggregation(urlsChild);

        sourceBuilder.aggregation(aggregation);

        request.source(sourceBuilder);
        SearchResponse response = search(request);
        if (response == null) {
            return new Page<>();
        }
        Page<InfoBO> page = new Page<>();
        List<InfoBO> result = new ArrayList<>();
        page.setCurrentPage(form.getCurrentPage());
        page.setPageSize(form.getPageSize());

        SearchHits hits = response.getHits();
        Terms terms = response.getAggregations().get(termName);
        page.setTotal((int) hits.getTotalHits().value);
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            InfoBO infoBO = new Gson().fromJson(json, InfoBO.class);
            terms.getBuckets().parallelStream()
                    .filter(b -> b.getKeyAsString().equals(form.getUrl())).forEach(b -> infoBO.setCount(b.getDocCount()));
            result.add(infoBO);
        }
        if (result.size() > 0) {
            page.setData(result.parallelStream()
                    .sorted(Comparator.comparing(InfoBO::getTimestamp).reversed()).collect(Collectors.toList()));
        }
        return page;
    }


    public List<InfoBO> urlChild(QueryBaseForm form) {
        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();

        sourceBuilder.query(getBoolQueryWithQueryForm(form));

        String topName = "top_score_hits";
        String childTermName = "urls_child";
        TermsAggregationBuilder urlsChild = AggregationBuilders.terms(childTermName).field("url_tpl.keyword");
        urlsChild.size(SIZE);


        TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(topName).size(1).sort("@timestamp", SortOrder.DESC);
        topHitsAggregationBuilder.fetchSource(form.isFullField() ? INCLUDE_SOURCE_ALL : INCLUDE_SOURCE, null);
        urlsChild.subAggregation(topHitsAggregationBuilder);


        sourceBuilder.aggregation(urlsChild);

        request.source(sourceBuilder);
        SearchResponse response = search(request);
        if (response == null) {
            return Collections.emptyList();
        }

        Terms terms = response.getAggregations().get(childTermName);
        List<InfoBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            long count = bucket.getDocCount();
            ParsedTopHits hits = bucket.getAggregations().get(topName);
            String json = hits.getHits().getAt(0).getSourceAsString();
            InfoBO bo = new Gson().fromJson(json, InfoBO.class);
            bo.setCount(count);
            result.add(bo);
        }
        return result;

    }

    private BoolQueryBuilder getBoolQueryWithQueryForm(QueryBaseForm form) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 处理过的数据
        boolQueryBuilder.must(QueryBuilders.termQuery("state", 1));

        // IP
        if (StringUtils.isNotBlank(form.getIp())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("ip", form.getIp()));
        }

        // 端口
        if (StringUtils.isNotBlank(form.getPort())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("port.keyword", form.getPort()));
        }

        // url
        if (StringUtils.isNotBlank(form.getUrl())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("site.keyword", form.getUrl()));
        }

        // 指纹
        if (StringUtils.isNotBlank(form.getFinger())) {
            boolQueryBuilder.must(QueryBuilders.fuzzyQuery("apps.name", form.getFinger().toLowerCase()));
        }

        // 分类ID
        if (form.getCategoryId() != null && !form.getCategoryId().isEmpty()) {
            BoolQueryBuilder tmpBoolQueryBuilder = QueryBuilders.boolQuery();
            for (Long id : form.getCategoryId()) {
                tmpBoolQueryBuilder.should(QueryBuilders.termQuery("apps.categories.id", id));
            }
            boolQueryBuilder.must(tmpBoolQueryBuilder);
        }

        // 筛选内网资产
        if (form.isInner()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("inner", form.isInner()));
        }

        // 时间范围
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp");
        if (form.getStart() != null) {
            rangeQueryBuilder.gte(form.getStart());
        }

        if (form.getEnd() != null) {
            rangeQueryBuilder.lte(form.getEnd());
        }

        // 时间都为空默认获取1天数据
        if (form.getStart() == null && form.getEnd() == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -1);
            rangeQueryBuilder.gte(c.getTime());
        }

        boolQueryBuilder.must(rangeQueryBuilder);

        return boolQueryBuilder;
    }

    private SearchResponse search(SearchRequest request) {
        try {
            log.debug("ES请求，地址: GET {}/_search, 内容: {}", request.indices()[0], request.source());
            return client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private SearchRequest getSearchRequest() {
        return new SearchRequest(index + "-*");
    }

    private SearchSourceBuilder getSourceBuilder() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        return sourceBuilder;
    }

}
