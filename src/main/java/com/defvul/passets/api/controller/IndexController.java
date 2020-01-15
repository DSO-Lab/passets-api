package com.defvul.passets.api.controller;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.res.*;
import com.defvul.passets.api.service.EsSearchService;
import com.defvul.passets.api.vo.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 说明:
 * 时间: 2019/11/8 14:31
 *
 * @author wimas
 */
@RestController
@Api(tags = "接口")
public class IndexController {

    @Autowired
    private EsSearchService esSearchService;

    @PostMapping("/ip")
    @ApiOperation(value = "查询IP+端口")
    public List<InfoBO> ip(@RequestBody QueryBaseForm form) {
        return esSearchService.queryTimeSlotWithIpAndPort(form);
    }

    @PostMapping("/ip/page")
    @ApiOperation(value = "分页查询IP+端口")
    public Page<InfoBO> ipPage(@RequestBody QueryBaseForm form) {
        return esSearchService.ipPage(form);
    }

    @PostMapping("/url")
    @ApiOperation(value = "查询站点+子链接")
    public List<UrlBO> url(@RequestBody QueryBaseForm form) {
        return esSearchService.queryTimeSlotWithUrl(form);
    }

    @PostMapping("/urls/page")
    @ApiOperation(value = "分页查询子链接")
    public Page<InfoBO> urlsPage(@RequestBody QueryBaseForm form) {
        return esSearchService.urlsPage(form);
    }

    @PostMapping("/url/all")
    @ApiOperation(value = "查询所有站点")
    public List<UrlBO> urlAll(@RequestBody QueryBaseForm form) {
        return esSearchService.urlAll(form);
    }

    @PostMapping("/url/child")
    @ApiOperation(value = "查询所有子链接")
    public List<InfoBO> urlChild(@RequestBody QueryBaseForm form) {
        return esSearchService.urlChild(form);
    }

    @PostMapping("/host/page")
    @ApiOperation(value = "主机列表")
    public Page<HostBO> host(@RequestBody QueryBaseForm form) {
        return esSearchService.host(form);
    }

    @GetMapping("/host/info")
    @ApiOperation(value = "主机列表")
    public HostBO infoHost(@RequestParam("value") String ip) {
        return esSearchService.infoHost(ip);
    }

    @PostMapping("/site/page")
    @ApiOperation(value = "站点列表")
    public Page<SiteBO> site(@RequestBody QueryBaseForm form) {
        return esSearchService.sitePage(form);
    }

    @GetMapping("/site/info")
    @ApiOperation(value = "站点详情")
    public SiteBO siteInfo(@RequestParam("value") String site) {
        return esSearchService.siteInfo(site);
    }

    @PostMapping("/host/top")
    @ApiOperation(value = "主机top")
    public TopBO hostTop(@RequestBody QueryBaseForm form) {
        return esSearchService.hostTop(form);
    }

    @PostMapping("/site/top")
    @ApiOperation(value = "站点top")
    public TopBO siteTop(@RequestBody QueryBaseForm form) {
        return esSearchService.siteTop(form);
    }
}
