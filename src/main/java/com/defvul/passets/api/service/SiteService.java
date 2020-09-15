package com.defvul.passets.api.service;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.req.QueryPageForm;
import com.defvul.passets.api.bo.res.*;
import com.defvul.passets.api.util.DateUtil;
import com.defvul.passets.api.vo.ApplicationVO;
import com.defvul.passets.api.vo.HostExportVO;
import com.defvul.passets.api.vo.Page;
import com.defvul.passets.api.vo.SiteExportVO;
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
        TermsAggregationBuilder urlsChildAgg = AggregationBuilders.terms(pathTermName).field("url_tpl.keyword").size(1000);
        // 时间统计
        StatsAggregationBuilder statsAggregationBuilder = AggregationBuilders.stats(stats).field("@timestamp");
        urlsChildAgg.subAggregation(statsAggregationBuilder);
        // 源数据
        TopHitsAggregationBuilder topHitsAgg = AggregationBuilders.topHits(topHits)
                .fetchSource(new String[]{"path", "title", "header", "body", "url_tpl"}, null).sort("@timestamp", SortOrder.DESC).size(1);
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
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(childTermName).field("url_tpl.keyword").size(1000);

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

    public void urlExport(QueryBaseForm form, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + "url.xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        List<String> urls = getUrl();
        int index = 500;
        int count = urls.size();
        List<SiteExportVO> vos = new ArrayList<>();
        if (urls.size() > 0) {
            for (int i = 0; i < urls.size(); i += 500) {
                if (i + 500 > count) {
                    index = count - i;
                }
                vos.addAll(urlBoToVo(getUrlBo(form, urls.subList(i, i + index))));
            }
        }
        try {
            ExcelUtils.getInstance().exportObjects2Excel(vos, SiteExportVO.class, true,
                    "URL资产", true, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<SiteExportBO> getUrlBo(QueryBaseForm form, List<String> urls) {
        String termName = "site_aggs";
        String urlTopHits = "site_top_hits";
        String urlTplTopHits = "site_url_tpl_hits";
        String appsName = "site_apps_term";
        String appsHit = "site_apps_hit";
        String pathTermName = "site_info_path";

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        sourceBuilder.query(esSearchService.getBoolQueryWithQueryForm(form))
                .query(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("site.keyword", urls)));

        // 站点聚合
        TermsAggregationBuilder urlsAgg = AggregationBuilders.terms(termName).field("site.keyword").size(EsSearchService.SIZE);
        TopHitsAggregationBuilder urlsTop = AggregationBuilders.topHits(urlTopHits).sort("@timestamp", SortOrder.DESC)
                .fetchSource(new String[]{"geoip", "inner", "@timestamp", "tag", "port", "ip", "header", "code"}, null).size(1);
        urlsAgg.subAggregation(urlsTop);

        // 末次更新时间
        MaxAggregationBuilder maxAgg = AggregationBuilders.max("timestamp_order").field("@timestamp");
        urlsAgg.subAggregation(maxAgg);
        // 模板聚合
        TermsAggregationBuilder urlsChildAgg = AggregationBuilders.terms(pathTermName).field("url_tpl.keyword").size(150);
        TopHitsAggregationBuilder urlTplTop = AggregationBuilders.topHits(urlTplTopHits).size(1);
        urlsChildAgg.subAggregation(urlTplTop);
        urlsAgg.subAggregation(urlsChildAgg);

        // 指纹聚合
        TermsAggregationBuilder appsTermsAggregationBuilder = AggregationBuilders.terms(appsName).field("apps.name.keyword").size(200);
        appsTermsAggregationBuilder.subAggregation(AggregationBuilders.topHits(appsHit).size(1).sort("@timestamp", SortOrder.DESC).fetchSource("apps", null));
        urlsAgg.subAggregation(appsTermsAggregationBuilder);

        sourceBuilder.aggregation(urlsAgg);
        SearchResponse response = esSearchService.search(sourceBuilder);

        if (response == null || response.getAggregations() == null) {
            return Collections.emptyList();
        }

        Terms urlTerms = response.getAggregations().get(termName);
        List<SiteExportBO> bos = new ArrayList<>();
        for (Terms.Bucket urlBucket : urlTerms.getBuckets()) {
            String url = urlBucket.getKeyAsString();
            SiteExportBO bo = esSearchService.getHitsByBucket(urlBucket, urlTopHits, SiteExportBO.class);
            bo.setSite(url);
            Set<String> paths = new HashSet<>();
            Terms urlTplTerms = urlBucket.getAggregations().get(pathTermName);
            for (Terms.Bucket urlTplBucket : urlTplTerms.getBuckets()) {
                paths.add(urlTplBucket.getKeyAsString());
            }
            bo.setPaths(paths);
//            System.out.println("URL：" + url + "字符长度=" + paths.size() + "---数量：" + paths.toString().length());
            Set<ApplicationVO> apps = new HashSet<>();
            Terms appsTerms = urlBucket.getAggregations().get(appsName);
            for (Terms.Bucket appsBucket : appsTerms.getBuckets()) {
                BaseInfoBO app = esSearchService.getHitsByBucket(appsBucket, appsHit, BaseInfoBO.class);
                if (app != null && !app.getApps().isEmpty()) {
                    apps.addAll(app.getApps());
                }
            }
            bo.setApps(new ArrayList<>(apps));

            Max time = urlBucket.getAggregations().get("timestamp_order");
            bo.setTimestamp(esSearchService.parseDate(time.getValueAsString()));

            bos.add(bo);
        }

        System.out.println("sourceBuilder = " + sourceBuilder);
        return bos;
    }

    private List<SiteExportVO> urlBoToVo(List<SiteExportBO> bos) {
        List<SiteExportVO> vos = new ArrayList<>();
        for (SiteExportBO bo : bos) {
            SiteExportVO vo = new SiteExportVO();
            BeanUtils.copyProperties(bo, vo, "inner", "timestamp");
            vo.setInner(bo.isInner() ? "内网" : "外网");
            for (ApplicationVO app : bo.getApps()) {
                if (StringUtils.isNotBlank(app.getName())) {
                    String version = StringUtils.isNotBlank(app.getVersion()) ? app.getVersion() : "未知";
                    String nameVersion = app.getName() + "(" + version + ")";
                    vo.getNameVersion().add(nameVersion);
                }
            }
            bo.getApps().stream().filter(a -> StringUtils.isNotBlank(a.getDevice())).forEach(a -> vo.setDevice(a.getDevice()));
            bo.getApps().stream().filter(a -> StringUtils.isNotBlank(a.getOs())).forEach(a -> vo.setOs(a.getOs()));
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
            vo.setPath(Strings.join(vo.getPaths(), ",\n"));
            vo.setVersion(vo.getNameVersion() != null ? Strings.join(vo.getNameVersion(), ",\n") : "");
            vo.setTimestamp(DateUtil.format(bo.getTimestamp(), DateUtil.YYYY_MM_DD_HH_MM_SS));
            vos.add(vo);

        }
        return vos;
    }

    private List<String> getUrl() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        TermsAggregationBuilder urlAgg = AggregationBuilders.terms("url_aggs").field("site.keyword").size(EsSearchService.SIZE);

        sourceBuilder.aggregation(urlAgg);
        System.out.println("sourceBuilder = " + sourceBuilder);

        SearchResponse response = esSearchService.search(sourceBuilder);
        List<String> urls = new ArrayList<>();
        if (response != null || response.getAggregations() != null) {
            Terms terms = response.getAggregations().get("url_aggs");
            for (Terms.Bucket bucket : terms.getBuckets()) {
                urls.add(bucket.getKeyAsString());
            }
        }
        return urls;
    }

//    public static void main(String[] args) {
//
//        //查询数据，实际可通过传过来的参数当条件去数据库查询，在此我就用空集合（数据）来替代
//        List<SiteExportVO> vos = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 10; j++) {
//                SiteExportVO vo = new SiteExportVO();
//                vo.setIp("1.1.1." + i);
//                vo.setPort("808" + i);
//                vo.setUrl("http://" + vo.getIp() + vo.getPort());
//                vo.setPath(vo.getUrl() + j);
//                vos.add(vo);
//            }
//        }
//        int count = 0, num = 0, sum = 0;
//        int firstRow = 1;
//        Map<String, RowNumber> map = new HashMap<>();
//
//        Set<String> urlSet = new HashSet<>();
//        vos.stream().forEach(v -> urlSet.add(v.getUrl()));
//        for (String vo : urlSet) {
//            int urlNum = 0;
//            RowNumber rowNumber = new RowNumber();
//            for (SiteExportVO vo1 : vos) {
//                if (vo.equals(vo1.getUrl())) {
//                    count++;
//                    urlNum++;
//                }
//            }
//            firstRow = firstRow + urlNum;
//            if (num == 0) {
//                rowNumber.setFirstRow(1);
//                rowNumber.setLastRow(count);
//            } else {
//                rowNumber.setFirstRow(vos.size() - sum + 1);
//                rowNumber.setLastRow(firstRow);
//            }
//            sum = vos.size() - count;
//
//            map.put(vo, rowNumber);
//            num++;
//        }
//        int index = 1;
//        for (String key : map.keySet()) {
//
//        }
//
//        //创建poi导出数据对象
//        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
//
//        //创建sheet页
//        SXSSFSheet sheet = sxssfWorkbook.createSheet("开复工项目");
//
//        CellRangeAddress region1 = new CellRangeAddress(0, 1, (short) 0, (short) 12);
//        //参数1：起始行 参数2：终止行 参数3：起始列 参数4：终止列
//        sheet.addMergedRegion(region1);
//        SXSSFRow headTitle = sheet.createRow(0);
//        headTitle.createCell(0).setCellValue("重点工程项目计划表");
//
//        //创建表头
//        SXSSFRow headRow = sheet.createRow(4);
//        //设置表头信息
//        headRow.createCell(0).setCellValue("URL");
//        headRow.createCell(1).setCellValue("IP");
//        headRow.createCell(2).setCellValue("端口");
//
//        headRow.createCell(3).setCellValue("访问路劲");
//        String yihui = null;
//
//        // 遍历上面数据库查到的数据
//        for (SiteExportVO vo : vos) {
//            SXSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
//            dataRow.createCell(0).setCellValue(vo.getUrl());
//            dataRow.createCell(1).setCellValue(vo.getIp());
//            dataRow.createCell(2).setCellValue(vo.getPort());
//
//            dataRow.createCell(3).setCellValue(vo.getPath());
//
//        }
////        // 下载导出
////        String filename = "XXXXXXX平台工程信息表";
////        // 设置头信息
////        response.setCharacterEncoding("UTF-8");
////        response.setContentType("application/vnd.ms-excel");
////        //一定要设置成xlsx格式
////        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", "UTF-8"));
////        //创建一个输出流
////        ServletOutputStream outputStream = response.getOutputStream();
////        //写入数据
////        sxssfWorkbook.write(outputStream);
////
////        // 关闭
////        outputStream.close();
////        sxssfWorkbook.close();
//    }
}
