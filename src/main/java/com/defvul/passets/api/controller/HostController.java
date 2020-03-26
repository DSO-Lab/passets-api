package com.defvul.passets.api.controller;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.req.QueryPageForm;
import com.defvul.passets.api.bo.res.HostBO;
import com.defvul.passets.api.bo.res.TopBO;
import com.defvul.passets.api.service.HostService;
import com.defvul.passets.api.vo.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 说明:
 * 时间: 2020/3/18 17:16
 *
 * @author wimas
 */
@RestController
@RequestMapping("/host")
@Api(tags = "主机")
public class HostController {
    @Autowired
    private HostService hostService;

    @PostMapping(value = "/page")
    @ApiOperation(value = "主机列表")
    public Page<HostBO> page(@RequestBody QueryPageForm form) {
        return hostService.page(form);
    }

    @PostMapping(value = "/info")
    @ApiOperation(value = "主机列表")
    public HostBO info(@RequestBody QueryInfoForm form) {
        return hostService.info(form);
    }

    @PostMapping(value = "/top")
    @ApiOperation(value = "主机top")
    public TopBO top(@RequestBody QueryBaseForm form) {
        return hostService.top(form);
    }

}
