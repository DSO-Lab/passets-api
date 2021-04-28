package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.res.TopBO;
import com.defvul.passets.api.bo.res.TopInfoBO;
import com.defvul.passets.api.util.IPUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    @Value("${query-tag-client}")
    private Boolean queryTagClient;

    public static final int SIZE = 2147483647;

//    private static final RequestOptions COMMON_OPTIONS;
//
//    static {
//        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(200 * 1024 * 1024));
//        COMMON_OPTIONS = builder.build();
//    }

    @PostConstruct
    public void init() {
        ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest();
        request.persistentSettings(new HashMap<String, Object>(1) {{
            put("search.max_buckets", SIZE);
        }});
        request.timeout(TimeValue.timeValueMinutes(2));
        try {
            Thread.sleep(20000L);
            client.cluster().putSettings(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            init();
            log.error("执行es设置报错: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public TopBO top(QueryBaseForm form, boolean isHost) {
        Map<String, String> maps = new HashMap<String, String>(7) {{
            put("pros", "pro.keyword");
            put("apps", "apps.name.keyword");
            put("inners", "inner");
            put("ports", "port");
            put("countrys", "geoip.country_name.keyword");
            put("os", "apps.os.keyword");
            put("tag", "tag.keyword");
        }};

        Map<String, List<TopInfoBO>> topInfoMap = new HashMap<>(maps.size());

        Set<Callable<String>> callables = new HashSet<>();
        for (String key : maps.keySet()) {
            callables.add(() -> {
                topInfoMap.put(key, topInfo(form, key, maps.get(key), isHost));
                return key;
            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(maps.size());
        try {
            executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();

        TopBO topBO = new TopBO();
        topBO.setPros(topInfoMap.get("pros"));
        topBO.setApps(topInfoMap.get("apps"));
        topBO.setInners(topInfoMap.get("inners"));
        topBO.setPorts(topInfoMap.get("ports"));
        topBO.setCountry(topInfoMap.get("countrys"));
        topBO.setOs(topInfoMap.get("os"));
        topBO.setTag(topInfoMap.get("tag"));

        return topBO;
    }

    private List<TopInfoBO> topInfo(QueryBaseForm form, String termName, String fieldName, boolean isHost) {
        String statsCount = "stats_count";

        SearchRequest request = getSearchRequest();
        SearchSourceBuilder sourceBuilder = getSourceBuilder();

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(termName).field(fieldName).size(SIZE);

        TermsAggregationBuilder statsCountAgg = AggregationBuilders.terms(statsCount).size(SIZE);

        if (isHost) {
            if ("pros".equals(termName)) {
                statsCountAgg.field("host.keyword");
            } else {
                statsCountAgg.field("ip_str.keyword");
            }
            sourceBuilder.query(getBoolQueryWithQueryForm(form));
        } else {
            statsCountAgg.field("site.keyword");
            BoolQueryBuilder boolQueryBuilder = getBoolQueryWithQueryForm(form);
            boolQueryBuilder.filter(QueryBuilders.boolQuery().should(QueryBuilders.existsQuery("site.keyword")));
            sourceBuilder.query(boolQueryBuilder);
        }

        termsAggregationBuilder.subAggregation(statsCountAgg);

        sourceBuilder.aggregation(termsAggregationBuilder);
        log.info(termName + "_top_json: {}", sourceBuilder);
        request.source(sourceBuilder);

        SearchResponse response = search(request);
        if (response == null || response.getAggregations() == null) {
            return Collections.emptyList();
        }
        Terms terms = response.getAggregations().get(termName);

        return setTopInfo(terms, statsCount);
    }

    private List<TopInfoBO> setTopInfo(Terms terms, String termName) {
        List<TopInfoBO> topList = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            TopInfoBO topInfo = new TopInfoBO();
            topInfo.setName(bucket.getKeyAsString());
            Terms nodeTerms = bucket.getAggregations().get(termName);
            topInfo.setCount(nodeTerms.getBuckets().size());
            topList.add(topInfo);
        }
        return limit(topList);
    }

    private List<TopInfoBO> limit(List<TopInfoBO> infoList) {
        if (infoList.size() <= 5) {
            return infoList;
        }
        return infoList.stream().sorted(Comparator.comparing(TopInfoBO::getCount).reversed()).limit(5L).collect(Collectors.toList());
    }

    public <T> T getHitsByBucket(Terms.Bucket bucket, String hitName, Class<T> clazz) {
        ParsedTopHits hits = bucket.getAggregations().get(hitName);
        return new Gson().fromJson(hits.getHits().getAt(0).getSourceAsString(), clazz);
    }

    public List<TopInfoBO> getInfoTopWithSearchResponse(SearchResponse response, String termName, String subTermName) {
        if (response == null || response.getAggregations() == null) {
            return Collections.emptyList();
        }
        Terms fingerTerms = response.getAggregations().get(termName);
        List<TopInfoBO> result = new ArrayList<>();
        for (Terms.Bucket bucket : fingerTerms.getBuckets()) {
            TopInfoBO bo = new TopInfoBO();
            bo.setName(bucket.getKeyAsString());
            if (StringUtils.isBlank(subTermName)) {
                bo.setCount(bucket.getDocCount());
            } else {
                Terms terms = bucket.getAggregations().get(subTermName);
                bo.setCount(terms.getBuckets().size());
            }
            result.add(bo);
        }
        return result.stream().sorted(Comparator.comparing(TopInfoBO::getCount).reversed()).collect(Collectors.toList());
    }

    public BoolQueryBuilder getBoolQueryWithQueryForm(QueryBaseForm form) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 处理过的数据
        boolQueryBuilder.filter(QueryBuilders.termQuery("state", 1));

        // 过滤Tag等于Client的
        if (!queryTagClient) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("tag", "client"));
        }

        // IP
        if (StringUtils.isNotBlank(form.getIp())) {
            boolean ipv6 = IPAddressUtil.isIPv6LiteralAddress(form.getIp());
            boolean ipv4 = IPUtils.isIp(form.getIp());
            if (ipv4 || ipv6) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("ip", form.getIp()));
            } else {
                boolQueryBuilder.filter(QueryBuilders.prefixQuery("ip_str", form.getIp()));
            }
        }

        // 端口
        if (StringUtils.isNotBlank(form.getPort())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("port", form.getPort()));
        }

        // site
        if (StringUtils.isNotBlank(form.getSite())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("site.keyword", form.getSite()));
        }

        // 来源
        if (StringUtils.isNoneBlank(form.getTag())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("tag.keyword", form.getTag()));
        }

        // url
        if (StringUtils.isNotBlank(form.getUrl())) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("url", form.getUrl().toLowerCase()).operator(Operator.AND));
        }

        // 指纹
        if (StringUtils.isNotBlank(form.getFinger())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("apps.name.keyword", form.getFinger()));
        }

        // 国家
        if (StringUtils.isNotBlank(form.getCountry())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("geoip.country_name.keyword", form.getCountry()));
        }

        // os
        if (StringUtils.isNotBlank(form.getOs())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("apps.os ", form.getOs()));
        }

        // os
        if (StringUtils.isNotBlank(form.getTitle())) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("title.keyword", form.getTitle()));
        }

        // type
        if (form.getPro() != null && !form.getPro().isEmpty()) {
            BoolQueryBuilder tmpBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String pro : form.getPro()) {
                tmpBoolQueryBuilder.should(QueryBuilders.termQuery("pro.keyword", pro));
            }
            boolQueryBuilder.filter(tmpBoolQueryBuilder);
        }

        // 分类ID
        if (form.getCategoryId() != null && !form.getCategoryId().isEmpty()) {
            BoolQueryBuilder tmpBoolQueryBuilder = QueryBuilders.boolQuery();
            for (Long id : form.getCategoryId()) {
                tmpBoolQueryBuilder.should(QueryBuilders.termQuery("apps.categories.id", id));
            }
            boolQueryBuilder.filter(tmpBoolQueryBuilder);
        }

        // 筛选内网资产
        if (form.getInner() != null && form.getInner().equals(1)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("inner", true));
        }

        // 筛选外网资产
        if (form.getInner() != null && form.getInner().equals(2)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("inner", false));
        }

        // 时间范围
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp");
        if (form.getStart() != null) {
            rangeQueryBuilder.gte(form.getStart().getTime());
        }

        if (form.getEnd() != null) {
            rangeQueryBuilder.lte(form.getEnd().getTime());
        }

        // 时间都为空默认获取1天数据
        if (form.getStart() == null && form.getEnd() == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -1);
            rangeQueryBuilder.gte(c.getTime().getTime());
        }

        boolQueryBuilder.filter(rangeQueryBuilder);

        return boolQueryBuilder;
    }

    public Date parseDate(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public SearchResponse search(SearchSourceBuilder builder) {
        SearchRequest request = getSearchRequest();
        request.source(builder);
        return search(request);
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
        return new SearchRequest(index + "-*");
    }

    public SearchSourceBuilder getPageSourceBuilder(int index, int size) {
        SearchSourceBuilder sourceBuilder = getSourceBuilder();
        int current = (index - 1) * size;
        sourceBuilder.from(current).size(size);
        return sourceBuilder;

    }

    public SearchSourceBuilder getSourceBuilder() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        return sourceBuilder;
    }


}
