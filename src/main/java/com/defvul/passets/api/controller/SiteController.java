package com.defvul.passets.api.controller;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.req.QueryPageForm;
import com.defvul.passets.api.bo.res.BaseInfoBO;
import com.defvul.passets.api.bo.res.SiteBO;
import com.defvul.passets.api.bo.res.TopBO;
import com.defvul.passets.api.service.SiteService;
import com.defvul.passets.api.vo.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 说明:
 * 时间: 2020/3/18 17:17
 *
 * @author wimas
 */
@RestController
@RequestMapping("/site")
@Api(tags = "站点")
public class SiteController {
    @Autowired
    private SiteService siteService;

    @PostMapping("/page")
    @ApiOperation(value = "站点列表")
    public Page<SiteBO> page(@RequestBody QueryPageForm form) {
        return siteService.page(form);
    }

    @PostMapping("/info")
    @ApiOperation(value = "站点详情")
    public SiteBO info(@RequestBody QueryInfoForm form) {
        return siteService.info(form);
    }

    @PostMapping("/top")
    @ApiOperation(value = "站点top")
    public TopBO top(@RequestBody QueryBaseForm form) {
        return siteService.top(form);
    }

    @PostMapping("/major/page")
    @ApiOperation(value = "重要站点分类")
    public Page<BaseInfoBO> majorPage(@RequestBody QueryPageForm form) {
        return siteService.majorPage(form);
    }
}
