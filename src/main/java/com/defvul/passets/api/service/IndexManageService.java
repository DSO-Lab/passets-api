package com.defvul.passets.api.service;

import com.defvul.passets.api.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 说明:
 * 时间: 2020/2/10 13:59
 *
 * @author wimas
 */
@Slf4j
@Service
public class IndexManageService {
    @Value("${elasticsearch.index}")
    private String index;

    @Value("${data-save-day}")
    private int expireDay;

    @Autowired
    private RestHighLevelClient client;

    public void deleteExpireIndex() {
        try {
            List<String> indexs = getAllExpireIndex();
            for (String i : indexs) {
                log.info("删除过期索引：{}", i);
                DeleteIndexRequest request = new DeleteIndexRequest(i);
                client.indices().delete(request, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            log.error("删除过期索引失败: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private List<String> getAllExpireIndex() throws IOException {
        GetAliasesResponse response = client.indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
        return response.getAliases().keySet().stream().filter(this::notExpire).collect(Collectors.toList());
    }

    private boolean notExpire(String indexStr) {
        if (!indexStr.startsWith(index)) {
            return false;
        }

        String[] is = indexStr.split("-");
        String min = DateUtil.format(DateUtil.add(new Date(), -expireDay), DateUtil.YYYYMMDD);
        return Long.parseLong(is[1]) < Long.parseLong(min);
    }
}
