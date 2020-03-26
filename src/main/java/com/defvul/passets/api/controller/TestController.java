package com.defvul.passets.api.controller;

import com.defvul.passets.api.service.IndexManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 说明:
 * 时间: 2020/2/10 14:02
 *
 * @author wimas
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private IndexManageService indexManageService;

    @GetMapping("/delete/index")
    public void deleteESIndex() {
        indexManageService.deleteExpireIndex();
    }
}

