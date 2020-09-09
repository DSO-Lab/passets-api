package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.req.QueryPageForm;
import com.defvul.passets.api.bo.res.*;
import com.defvul.passets.api.util.CommonUtil;
import com.defvul.passets.api.vo.ApplicationVO;
import com.defvul.passets.api.vo.HostExportVO;
import com.defvul.passets.api.vo.Page;
import com.github.crab2died.ExcelUtils;
import com.google.gson.Gson;
import joptsimple.internal.Strings;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 说明:
 * 时间: 2020/3/18 17:29
 *
 * @author wimas
 */
@Slf4j
@Service
public class HostService {
    @Autowired
    private EsSearchService esSearchService;

    private static final String[] INCLUDE_SOURCE = new String[]{
            "ip",
            "inner",
            "geoip"
    };

    private static final String[] INCLUDE_SOURCE_INFO = new String[]{
            "@timestamp",
            "ip",
            "pro",
            "port",
            "site",
            "apps",
            "url",
            "tag",
            "title",
    };

    private static final String[] INCLUDE_APPS = new String[]{
            "apps"
    };


    /**
     * 分页查询 IP资产
     *
     * @param form
     * @return
     */
    public Page<HostBO> page(QueryPageForm form) {
        Page<HostBO> page = form.toPage();
        // 如果端口不正确， 返回空数据
        if (StringUtils.isNoneBlank(form.getPort()) && !CommonUtil.isPort(form.getPort())) {
            return page;
        }
        page.setData(query(form, page));
        return page;
    }

    /**
     * 资产详情
     *
     * @param form
     * @return
     */
    public HostBO info(QueryInfoForm form) {
        if (StringUtils.isBlank(form.getValue())) {
            return null;
        }
        QueryBaseForm baseForm = form.toBaseForm(true);
        List<HostBO> list = query(baseForm, null);
        HostBO hostBO = new HostBO();
        if (!list.isEmpty()) {
            hostBO = list.get(0);
            List<String> ports = hostBO.getHosts().parallelStream().map(HostInfoBO::getPort).collect(Collectors.toList());
            hostBO.setPorts(ports);
            hostBO.setAssembly(queryInfoTopWithFinger(baseForm));
        }
        return hostBO;
    }

    /**
     * 主机资产分类排名
     *
     * @param form
     * @return
     */
    public TopBO top(QueryBaseForm form) {
        return esSearchService.top(form, true);
    }

    private List<TopInfoBO> queryInfoTopWithFinger(QueryBaseForm form) {
        String portInfo = "host_port_top";
        String fingerInfo = "host_finger_top";
        SearchSourceBuilder sourceBuilder = esSearchService.getSourceBuilder();
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder appNameAgg = AggregationBuilders.terms(fingerInfo).field("apps.name.keyword").size(EsSearchService.SIZE);

        // 聚合指纹的端口，以端口数量为指纹统计数量
        TermsAggregationBuilder portAgg = AggregationBuilders.terms(portInfo).field("port").size(EsSearchService.SIZE);
        appNameAgg.subAggregation(portAgg);
        sourceBuilder.aggregation(appNameAgg);

        log.info("host_info_top_finger_query: {}", sourceBuilder);
        return esSearchService.getInfoTopWithSearchResponse(esSearchService.search(sourceBuilder), fingerInfo, portInfo);
    }

    private List<HostBO> query(QueryBaseForm form, Page page) {
        String termName = "host_info";
        boolean notPage = page == null;
        SearchSourceBuilder sourceBuilder = notPage
                ? esSearchService.getSourceBuilder()
                : esSearchService.getPageSourceBuilder(page.getCurrentPage(), page.getPageSize());
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form));
        if (notPage) {
            sourceBuilder.size(1);
        }
        sourceBuilder.sort("@timestamp", SortOrder.DESC);
        sourceBuilder.fetchSource(INCLUDE_SOURCE, null).collapse(new CollapseBuilder("ip_str.keyword"));
        TermsAggregationBuilder ipsAgg = AggregationBuilders.terms(termName).field("ip").size(EsSearchService.SIZE);
        ipsAgg.order(BucketOrder.aggregation("timestamp_order", false));

        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("timestamp_order").field("@timestamp");
        ipsAgg.subAggregation(maxAggregationBuilder);
        sourceBuilder.aggregation(ipsAgg);
        log.info("host_page_query: {}", sourceBuilder);
        SearchResponse response = esSearchService.search(sourceBuilder);
        if (response == null) {
            return Collections.emptyList();
        }
        SearchHits searchHits = response.getHits();
        if (response.getAggregations() != null && page != null) {
            Terms terms = response.getAggregations().get(termName);
            page.setTotal(terms.getBuckets().size());
        }

        List<HostBO> result = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            HostBO hostBO = new Gson().fromJson(searchHit.getSourceAsString(), HostBO.class);
            form.setIp(hostBO.getIp());
            hostBO.setHosts(queryInfo(form));
            result.add(hostBO);
        }
        return result;
    }

    private List<HostInfoBO> queryInfo(QueryBaseForm form) {
        String termName = "host_info";
        String stats = "host_info_times";
        String topHitsName = "host_info_hits";
        String titleFilter = "host_title_filter";
        String titleCount = "host_title_count";
        String appsName = "host_apps_term";
        String appsHit = "host_apps_hit";

        SearchSourceBuilder sourceBuilder = esSearchService.getSourceBuilder();
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form));
        sourceBuilder.sort("@timestamp", SortOrder.DESC);

        // 端口聚合
        TermsAggregationBuilder portAgg = AggregationBuilders.terms(termName).field("port").size(EsSearchService.SIZE);

        // 源数据
        TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(topHitsName).size(1).sort("@timestamp", SortOrder.DESC);
        topHitsAggregationBuilder.fetchSource(INCLUDE_SOURCE_INFO, null);
        // 时间统计
        StatsAggregationBuilder statsAggregationBuilder = AggregationBuilders.stats(stats).field("@timestamp");
        // 标题聚合
        FilterAggregationBuilder filterAggregationBuilder = AggregationBuilders.filter(titleFilter,
                QueryBuilders.boolQuery().must(QueryBuilders.termQuery("code", "200"))
        );
        filterAggregationBuilder.subAggregation(AggregationBuilders.terms(titleCount).field("title.keyword").size(1));
        // 组件聚合
        TermsAggregationBuilder appsTermsAggregationBuilder = AggregationBuilders.terms(appsName).field("apps.name.keyword").size(EsSearchService.SIZE);
        appsTermsAggregationBuilder.subAggregation(AggregationBuilders.topHits(appsHit).size(1).sort("@timestamp", SortOrder.DESC).fetchSource(INCLUDE_APPS, null));
        // 端口聚合 -> 子聚合
        portAgg.subAggregation(topHitsAggregationBuilder)
                .subAggregation(statsAggregationBuilder)
                .subAggregation(filterAggregationBuilder)
                .subAggregation(appsTermsAggregationBuilder);

        sourceBuilder.aggregation(portAgg);
        log.info("host_info_query: {}", sourceBuilder);
        SearchResponse response = esSearchService.search(sourceBuilder);
        if (response == null || response.getAggregations() == null) {
            return Collections.emptyList();
        }
        Terms terms = response.getAggregations().get(termName);
        List<HostInfoBO> hostInfoBOList = new ArrayList<>();
        if (terms != null) {
            for (Terms.Bucket portTerms : terms.getBuckets()) {
                // 源数据
                ParsedTopHits hits = portTerms.getAggregations().get(topHitsName);
                HostInfoBO infoBO = new Gson().fromJson(hits.getHits().getAt(0).getSourceAsString(), HostInfoBO.class);
                infoBO.setCount(portTerms.getDocCount());
                // 时间
                ParsedStats timeStats = portTerms.getAggregations().get(stats);
                infoBO.setMinDate(esSearchService.parseDate(timeStats.getMinAsString()));
                infoBO.setMaxDate(esSearchService.parseDate(timeStats.getMaxAsString()));
                // title
                ParsedFilter titleParsed = portTerms.getAggregations().get(titleFilter);
                Terms titleTerms = titleParsed.getAggregations().get(titleCount);
                if (titleTerms != null && !titleTerms.getBuckets().isEmpty() && StringUtils.isBlank(infoBO.getTitle())) {
                    infoBO.setTitle(titleTerms.getBuckets().get(0).getKeyAsString());
                }
                // 组件
                Terms appsTerms = portTerms.getAggregations().get(appsName);
                if (!appsTerms.getBuckets().isEmpty()) {
                    Set<ApplicationVO> appSets = new HashSet<>();
                    for (Terms.Bucket appsBucket : appsTerms.getBuckets()) {
                        BaseInfoBO vo = esSearchService.getHitsByBucket(appsBucket, appsHit, BaseInfoBO.class);
                        if (vo != null && !vo.getApps().isEmpty()) {
                            appSets.addAll(vo.getApps());
                        }
                    }
                    infoBO.setApps(new ArrayList<>(appSets));
                }
                hostInfoBOList.add(infoBO);
            }
        }
        return hostInfoBOList.parallelStream().sorted(Comparator.comparing(HostInfoBO::getMaxDate).reversed()).collect(Collectors.toList());
    }

    public void ipExport(QueryBaseForm form, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + "ip.xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        List<HostExportVO> vos = ipBoToVo(getIpBo(form));
        try {
            ExcelUtils.getInstance().exportObjects2Excel(vos, HostExportVO.class, true,
                    "IP资产", true, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<HostExportBO> getIpBo(QueryBaseForm form) {
        String ipAggs = "ip_aggs";
        String portAggs = "port_aggs";
        String ipPortHits = "ip_port_hits";
        String appsAggs = "apps_aggs";
        String appsTopHits = "apps_top_hits";

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form));

        TermsAggregationBuilder ipsAgg = AggregationBuilders.terms(ipAggs).field("ip_str.keyword").size(EsSearchService.SIZE);

        TermsAggregationBuilder portAgg = AggregationBuilders.terms(portAggs).field("port").size(EsSearchService.SIZE);
        ipsAgg.subAggregation(portAgg);

        TopHitsAggregationBuilder ipPortTopHitsAgg = AggregationBuilders.topHits(ipPortHits).size(1)
                .sort("@timestamp", SortOrder.DESC).fetchSource(new String[]{"geoip", "inner", "@timestamp", "certs", "tag"}, null);
        portAgg.subAggregation(ipPortTopHitsAgg);

        TermsAggregationBuilder appsTermsAggregationBuilder = AggregationBuilders.terms(appsAggs).field("apps.name.keyword").size(200);
        appsTermsAggregationBuilder.subAggregation(AggregationBuilders.topHits(appsTopHits).size(1).sort("@timestamp", SortOrder.DESC).fetchSource("apps", null));
        portAgg.subAggregation(appsTermsAggregationBuilder);

        sourceBuilder.aggregation(ipsAgg);
        System.out.println("sourceBuilder = " + sourceBuilder);

        long star = System.currentTimeMillis();
        SearchResponse searchResponse = esSearchService.search(sourceBuilder);
        if (searchResponse == null || searchResponse.getAggregations() == null) {
            return Collections.emptyList();
        }
        Terms terms = searchResponse.getAggregations().get(ipAggs);
        List<HostExportBO> bos = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            String ip = bucket.getKeyAsString();
            Terms portTerms = bucket.getAggregations().get(portAggs);
            if (!portTerms.getBuckets().isEmpty()) {
                for (Terms.Bucket portBucket : portTerms.getBuckets()) {
                    String port = portBucket.getKeyAsString();

                    HostExportBO bo = esSearchService.getHitsByBucket(portBucket, ipPortHits, HostExportBO.class);
                    bo.setIp(ip);
                    bo.setPort(port);
                    List<ApplicationVO> apps = new ArrayList<>();
                    Terms appsTerms = portBucket.getAggregations().get(appsAggs);
                    for (Terms.Bucket appsBucket : appsTerms.getBuckets()) {
                        ApplicationVO app = esSearchService.getHitsByBucket(appsBucket, appsTopHits, ApplicationVO.class);
                        apps.add(app);
                    }
                    bo.setApps(apps);
                    bos.add(bo);
                }
            }
        }
        long end = System.currentTimeMillis();
        long oo = (end - star);
        System.out.println("查询资产花费： " + oo + "毫秒");
        return bos;
    }

    private List<HostExportVO> ipBoToVo(List<HostExportBO> bos) {
        List<HostExportVO> vos = new ArrayList<>();
        for (HostExportBO bo : bos) {
            HostExportVO vo = new HostExportVO();
            BeanUtils.copyProperties(bo, vo, "inner");
            vo.setInner(bo.isInner() ? "内网" : "外网");
            for (ApplicationVO app : bo.getApps()) {
                if (StringUtils.isNotBlank(app.getName())) {
                    String version = StringUtils.isNotBlank(app.getVersion()) ? app.getVersion() : "未知";
                    String nameVersion = app.getName() + "(" + version + ")";
                    vo.getNameVersion().add(nameVersion);
                }
            }
            bo.getApps().stream().filter(a -> StringUtils.isNotBlank(a.getDevice())).limit(1).forEach(a -> vo.setDevice(a.getDevice()));
            bo.getApps().stream().filter(a -> StringUtils.isNotBlank(a.getService())).limit(1).forEach(a -> vo.setService(a.getService()));
            bo.getApps().stream().filter(a -> StringUtils.isNotBlank(a.getOs())).limit(1).forEach(a -> vo.setOs(a.getOs()));
            if (bo.getGeoIp() != null) {
                String countryName = null;
                if (StringUtils.isNotBlank(bo.getGeoIp().getCountryName())) {
                    countryName = bo.getGeoIp().getCountryName();

                }
                String city = null;
                if (StringUtils.isNotBlank(bo.getGeoIp().getCityName())) {
                    city = bo.getGeoIp().getCityName();
                }
                vo.setPosition((countryName == null ? "" : countryName) + (city == null ? "" : city));
                if (bo.getGeoIp().getLocation() != null) {
                    vo.setDegree(bo.getGeoIp().getLocation().getLon() + "," + bo.getGeoIp().getLocation().getLat());
                }
            }
            vo.setVersion(vo.getNameVersion() != null ? Strings.join(vo.getNameVersion(), ",") : "");
            vos.add(vo);

        }
        return vos;
    }

    private List<String> getIp() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        TermsAggregationBuilder ipsAgg = AggregationBuilders.terms("ip_aggs").field("ip_str.keyword").size(EsSearchService.SIZE);

        sourceBuilder.aggregation(ipsAgg);
        System.out.println("sourceBuilder = " + sourceBuilder);

        SearchResponse response = esSearchService.search(sourceBuilder);
        List<String> ips = new ArrayList<>();
        if (response != null || response.getAggregations() != null) {
            Terms terms = response.getAggregations().get("ip_aggs");
            for (Terms.Bucket bucket : terms.getBuckets()) {
                ips.add(bucket.getKeyAsString());
            }
        }
        return ips;
    }
}
