package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.req.QueryPageForm;
import com.defvul.passets.api.bo.res.*;
import com.defvul.passets.api.vo.ApplicationVO;
import com.defvul.passets.api.vo.Page;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 说明:
 * 时间: 2020/3/18 17:47
 *
 * @author wimas
 */
@Slf4j
@Service
public class SiteService {
    @Autowired
    private EsSearchService esSearchService;

    private static final String[] INCLUDE_SOURCE = new String[]{
            "ip",
            "host",
            "port",
            "inner",
            "site",
            "title",
            "@timestamp",
            "tag",
    };

    private static final String[] INCLUDE_SOURCE_INFO = new String[]{
            "ip",
            "path",
            "host",
            "port",
            "site",
            "apps",
            "title",
            "header",
            "body",
            "url",
            "url_tpl",
            "@timestamp",
            "tag",
    };


    private static final String[] INCLUDE_SOURCE_MAJOR = new String[]{
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
            "tag",
    };

    /**
     * 分页查询，站点资产
     *
     * @param form
     * @return
     */
    public Page<SiteBO> page(QueryPageForm form) {
        Page<SiteBO> page = form.toPage();
        page.setData(query(form, page));
        return page;
    }

    /**
     * 资产详情
     *
     * @param form
     * @return
     */
    public SiteBO info(QueryInfoForm form) {
        if (StringUtils.isBlank(form.getValue())) {
            return null;
        }
        QueryBaseForm baseForm = form.toBaseForm(false);
        List<SiteBO> list = query(baseForm, null);
        SiteBO siteBO = new SiteBO();
        if (!list.isEmpty()) {
            siteBO = list.get(0);
            siteBO.setApps(queryInfoTop(baseForm));
            siteBO.setSites(queryInfo(baseForm));
        }
        return siteBO;
    }

    /**
     * 资产分类排名
     *
     * @param form
     * @return
     */
    public TopBO top(QueryBaseForm form) {
        return esSearchService.top(form, false);
    }


    private List<TopInfoBO> queryInfoTop(QueryBaseForm form) {
        String termName = "site_info_top";
        String tplName = "site_tpl_term";
        SearchSourceBuilder sourceBuilder = esSearchService.getSourceBuilder();
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form));
        TermsAggregationBuilder appNameAgg = AggregationBuilders.terms(termName).field("apps.name.keyword").size(EsSearchService.SIZE);
        TermsAggregationBuilder tplAgg = AggregationBuilders.terms(tplName).field("url_tpl.keyword").size(EsSearchService.SIZE);
        appNameAgg.subAggregation(tplAgg);
        sourceBuilder.aggregation(appNameAgg);
        log.info("site_info_top_query: {}", sourceBuilder);
        return esSearchService.getInfoTopWithSearchResponse(esSearchService.search(sourceBuilder), termName, tplName);
    }

    private List<SiteBO> query(QueryBaseForm form, Page page) {
        String termName = "site_keyword";
        String titleFilter = "site_title_filter";
        String titleCount = "site_title_count";
        String appsName = "site_apps_term";
        String appsHit = "site_apps_hit";
        String pathTermName = "site_info_path";

        boolean notPage = page == null;
        SearchSourceBuilder sourceBuilder = notPage
                ? esSearchService.getSourceBuilder()
                : esSearchService.getPageSourceBuilder(page.getCurrentPage(), page.getPageSize());
        BoolQueryBuilder boolQueryBuilder = esSearchService.getBoolQueryWithQueryForm(form);
        if (notPage) {
            sourceBuilder.size(1);
        } else {
            boolQueryBuilder.filter(QueryBuilders.termQuery("pro.keyword", "HTTP"));
        }

        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.sort("@timestamp", SortOrder.DESC);
        sourceBuilder.fetchSource(INCLUDE_SOURCE, null)
                .collapse(new CollapseBuilder("site.keyword"));

        // 站点聚合
        TermsAggregationBuilder urlsAgg = AggregationBuilders.terms(termName).field("site.keyword").size(EsSearchService.SIZE);
        urlsAgg.order(BucketOrder.aggregation("timestamp_order", false));

        // 末次更新时间
        MaxAggregationBuilder maxAgg = AggregationBuilders.max("timestamp_order").field("@timestamp");
        urlsAgg.subAggregation(maxAgg);
        // 模板聚合
        TermsAggregationBuilder urlsChildAgg = AggregationBuilders.terms(pathTermName).field("url_tpl.keyword").size(EsSearchService.SIZE);
        urlsAgg.subAggregation(urlsChildAgg);
        // 标题聚合
        FilterAggregationBuilder filterAggregationBuilder = AggregationBuilders.filter(titleFilter,
                QueryBuilders.boolQuery().must(QueryBuilders.termQuery("code", "200"))
        );
        filterAggregationBuilder.subAggregation(AggregationBuilders.terms(titleCount).field("title.keyword").size(1));
        urlsAgg.subAggregation(filterAggregationBuilder);
        // 组件聚合
        TermsAggregationBuilder appsTermsAggregationBuilder = AggregationBuilders.terms(appsName).field("apps.name.keyword").size(EsSearchService.SIZE);
        appsTermsAggregationBuilder.subAggregation(AggregationBuilders.topHits(appsHit).size(1).sort("@timestamp", SortOrder.DESC));
        urlsAgg.subAggregation(appsTermsAggregationBuilder);

        sourceBuilder.aggregation(urlsAgg);

        log.info("site_page_query: {}", sourceBuilder);
        SearchResponse response = esSearchService.search(sourceBuilder);
        if (response == null) {
            return Collections.emptyList();
        }


        Terms terms = response.getAggregations().get(termName);
        if (!notPage) {
            page.setTotal(terms.getBuckets().size());
        }

        log.info("开始设置（子url,title,组件）");

        Map<String, Terms.Bucket> bucketMap = new HashMap<>(terms.getBuckets().size());
        for (Terms.Bucket bucket : terms.getBuckets()) {
            bucketMap.put(bucket.getKeyAsString().toLowerCase(), bucket);
        }

        SearchHits searchHits = response.getHits();
        List<SiteBO> siteList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            SiteBO bo = new Gson().fromJson(searchHit.getSourceAsString(), SiteBO.class);
            Terms.Bucket bucket = bucketMap.get(bo.getSite().toLowerCase());
            bo.setCount(bucket.getDocCount());
            // 子url数
            Terms pathTerm = bucket.getAggregations().get(pathTermName);
            bo.setUrlNum(pathTerm.getBuckets().size());
            // title
            ParsedFilter titleParsed = bucket.getAggregations().get(titleFilter);
            Terms titleTerms = titleParsed.getAggregations().get(titleCount);
            if (!titleTerms.getBuckets().isEmpty() && StringUtils.isBlank(bo.getTitle())) {
                bo.setTitle(titleTerms.getBuckets().get(0).getKeyAsString());
            }
            Set<ApplicationVO> apps = new HashSet<>();
            // 组件
            Terms appsTerms = bucket.getAggregations().get(appsName);
            if (!appsTerms.getBuckets().isEmpty()) {
                for (Terms.Bucket appsBucket : appsTerms.getBuckets()) {
                    BaseInfoBO vo = esSearchService.getHitsByBucket(appsBucket, appsHit, BaseInfoBO.class);
                    if (vo != null && !vo.getApps().isEmpty()) {
                        apps.addAll(vo.getApps());
                    }
                }
            }
            bo.setApp(new ArrayList<>(apps));
            siteList.add(bo);
        }
        log.info("结束设置（子url,title,组件）");

        return siteList.parallelStream().sorted(Comparator.comparing(SiteBO::getTimestamp).reversed()).collect(Collectors.toList());
    }

    private List<SiteInfoBO> queryInfo(QueryBaseForm form) {
        String pathTermName = "site_info_path";
        String topHits = "site_score_hits";
        String stats = "site_info_times";


        SearchSourceBuilder sourceBuilder = esSearchService.getSourceBuilder();
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form));
        sourceBuilder.sort("@timestamp", SortOrder.DESC);

        // 用url_tpl进行聚合计算
        TermsAggregationBuilder urlsChildAgg = AggregationBuilders.terms(pathTermName).field("url_tpl.keyword").size(EsSearchService.SIZE);
        // 时间统计
        StatsAggregationBuilder statsAggregationBuilder = AggregationBuilders.stats(stats).field("@timestamp");
        urlsChildAgg.subAggregation(statsAggregationBuilder);
        // 源数据
        TopHitsAggregationBuilder topHitsAgg = AggregationBuilders.topHits(topHits)
                .fetchSource(INCLUDE_SOURCE_INFO, null).sort("@timestamp", SortOrder.DESC).size(1);
        urlsChildAgg.subAggregation(topHitsAgg);
        sourceBuilder.aggregation(urlsChildAgg);

        log.info("site_info_query: {}", sourceBuilder);
        SearchResponse response = esSearchService.search(sourceBuilder);
        if (response == null || response.getAggregations() == null) {
            return Collections.emptyList();
        }

        List<SiteInfoBO> siteInfoList = new ArrayList<>();

        Terms terms = response.getAggregations().get(pathTermName);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            SiteInfoBO bo = esSearchService.getHitsByBucket(bucket, topHits, SiteInfoBO.class);
            // 时间
            ParsedStats timeStats = bucket.getAggregations().get(stats);
            bo.setMinDate(esSearchService.parseDate(timeStats.getMinAsString()));
            bo.setMaxDate(esSearchService.parseDate(timeStats.getMaxAsString()));
            siteInfoList.add(bo);
        }
        return siteInfoList;
    }

    /**
     * 重要站点分页
     *
     * @param form
     * @return
     */
    public Page<BaseInfoBO> majorPage(QueryPageForm form) {
        Page<BaseInfoBO> page = form.toPage();
        SearchSourceBuilder sourceBuilder = esSearchService.getPageSourceBuilder(page.getCurrentPage(), page.getPageSize());
        BoolQueryBuilder boolQueryBuilder = esSearchService.getBoolQueryWithQueryForm(form);
        boolQueryBuilder.filter(QueryBuilders.termQuery("pro.keyword", "HTTP"));
        sourceBuilder.query(boolQueryBuilder);

        String topName = "top_score_hits";
        String childTermName = "urls_child";
        sourceBuilder.sort("@timestamp", SortOrder.DESC);
        sourceBuilder.fetchSource(INCLUDE_SOURCE_MAJOR, null).collapse(new CollapseBuilder("url_tpl.keyword"));
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(childTermName).field("url_tpl.keyword").size(EsSearchService.SIZE);

        sourceBuilder.aggregation(termsAggregationBuilder);

        log.info("site_major_query: {}", sourceBuilder);
        SearchResponse response = esSearchService.search(sourceBuilder);
        if (response == null || response.getAggregations() == null) {
            return page;
        }
        Terms terms = response.getAggregations().get(childTermName);
        page.setTotal(terms.getBuckets().size());
        Map<String, Long> countMap = new HashMap<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            countMap.put(bucket.getKeyAsString().toLowerCase(), bucket.getDocCount());
        }
        List<BaseInfoBO> rows = new ArrayList<>();
        for (SearchHit searchHit : response.getHits()) {
            BaseInfoBO bo = new Gson().fromJson(searchHit.getSourceAsString(), BaseInfoBO.class);
            if (bo != null && bo.getUrlTpl() != null) {
                bo.setCount(countMap.get(bo.getUrlTpl().toLowerCase()));
                rows.add(bo);
            }
        }
        page.setData(rows);

        return page;
    }
}
