package com.defvul.passets.api.vo;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

public class CustomMergeStrategy extends AbstractMergeStrategy {

    private Map<Integer, List<RowRange>> strategyMap;

    private Sheet sheet;

    public CustomMergeStrategy(Map<Integer, List<RowRange>> strategyMap) {
        this.strategyMap = strategyMap;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        this.sheet = sheet;
        if (cell.getRowIndex() == 1 && cell.getColumnIndex() == 0) {
            for (Map.Entry<Integer, List<RowRange>> entry : strategyMap.entrySet()) {
                Integer columnIndex = entry.getKey();
                entry.getValue().forEach(r -> {
                    sheet.addMergedRegionUnsafe(new CellRangeAddress(r.getStart(), r.getEnd(), columnIndex, columnIndex));
                });
            }
        }
    }
}
