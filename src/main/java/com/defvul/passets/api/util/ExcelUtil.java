package com.defvul.passets.api.util;

import com.defvul.passets.api.vo.RowRange;
import com.defvul.passets.api.vo.SiteMergeStartegyExportVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    public static Map<Integer, List<RowRange>> addMergeStrategy(List<SiteMergeStartegyExportVO> list) {

        Map<Integer, List<RowRange>> map = new HashMap<>();
        SiteMergeStartegyExportVO upVo = null;
        for (int i = 0; i < list.size(); i++) {
            SiteMergeStartegyExportVO currVo = list.get(i);
            if (upVo != null) {
                if (currVo.getSite().equals(upVo.getSite())) {
                    fillStrategyMap(map, 0, i);
                    fillStrategyMap(map, 1, i);
                    fillStrategyMap(map, 2, i);
                    fillStrategyMap(map, 3, i);
                    fillStrategyMap(map, 4, i);
                    fillStrategyMap(map, 5, i);
                    fillStrategyMap(map, 6, i);
                    fillStrategyMap(map, 7, i);
                    fillStrategyMap(map, 8, i);
                    fillStrategyMap(map, 9, i);
                    fillStrategyMap(map, 10, i);
                    fillStrategyMap(map, 11, i);
                }
            }
            upVo = currVo;
        }
        return map;
    }

    private static void fillStrategyMap(Map<Integer, List<RowRange>> strategyMap, Integer key, int index) {
        List<RowRange> rowRangeList = strategyMap.get(key) == null ? new ArrayList<>() : strategyMap.get(key);
        boolean flag = false;
        for (RowRange dto : rowRangeList) {
            //分段list中是否有end索引是上一行索引的，如果有，则索引+1
            if (dto.getEnd() == index) {
                dto.setEnd(index + 1);
                flag = true;
            }
        }
        //如果没有，则新增分段
        if (!flag) {
            rowRangeList.add(new RowRange(index, index + 1));
        }
        strategyMap.put(key, rowRangeList);
    }
}
