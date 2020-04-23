package com.defvul.passets.api.controller;

import com.defvul.passets.api.bo.req.QueryBaseForm;
import com.defvul.passets.api.bo.req.QueryInfoForm;
import com.defvul.passets.api.bo.res.BaseInfoBO;
import com.defvul.passets.api.service.ApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 说明:
 * 时间: 2020/3/25 14:16
 *
 * @author wimas
 */
@RestController
@RequestMapping("/api")
@Api(tags = "接口")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @PostMapping("/host")
    @ApiOperation(value = "查看ip+port")
    private List<BaseInfoBO> host(@RequestBody QueryBaseForm form) {
        return apiService.queryHosts(form);
    }

    @PostMapping("/urls")
    @ApiOperation(value = "根据site查询所有url")
    private List<BaseInfoBO> urls(@RequestBody QueryInfoForm form) {
        return apiService.queryUrls(form);
    }

    @GetMapping("/connection")
    @ApiOperation(value = "测试连接")
    private boolean testConnection() {
        return true;
    }
}
