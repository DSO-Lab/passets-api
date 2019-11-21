package com.defvul.passets.api.controller;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryUrlForm;
import com.defvul.passets.api.bo.res.IpPortBO;
import com.defvul.passets.api.bo.res.UrlBO;
import com.defvul.passets.api.bo.res.UrlInfoBO;
import com.defvul.passets.api.service.EsSearchService;
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
public class IndexController extends BaseController {

    @Autowired
    private EsSearchService esSearchService;

    @PostMapping("/ip")
    @ApiOperation(value = "查询IP+端口")
    public List<IpPortBO> ip(@RequestBody QueryBaseForm form) {
        return esSearchService.queryTimeSlotWithIpAndPort(form);
    }

    @PostMapping("/url")
    @ApiOperation(value = "查询Host和URL")
    public List<UrlBO> url(@RequestBody QueryBaseForm form) {
        return esSearchService.queryTimeSlotWithUrl(form);
    }

    @PostMapping("/url/query")
    @ApiOperation(value = "根据URL查询")
    public List<UrlInfoBO> urlInfo(@RequestBody QueryUrlForm form) {
        return esSearchService.queryByUrl(form);
    }

}
