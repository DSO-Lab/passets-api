package com.defvul.passets.api.scheduler;

import com.defvul.passets.api.service.IndexManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 说明:
 * 时间: 2019/12/23 11:27
 *
 * @author wimas
 */
@Slf4j
@Service
public class IndexManageScheduler {
    @Autowired
    private IndexManageService indexManageService;

    /**
     * 每天晚上24点执行删除过期索引
     */
    @Scheduled(cron = "1 0 0 * * ?")
    public void deleteExpireIndex() {
        log.info("定时器：处理删除过期索引。");
        indexManageService.deleteExpireIndex();
    }

}
