package com.defvul.passets.api.controller;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.res.InfoBO;
import com.defvul.passets.api.bo.res.UrlBO;
import com.defvul.passets.api.service.EsSearchService;
import com.defvul.passets.api.vo.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
