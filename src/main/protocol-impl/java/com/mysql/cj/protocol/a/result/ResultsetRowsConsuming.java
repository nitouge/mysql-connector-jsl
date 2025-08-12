package com.mysql.cj.protocol.a.result;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.result.Row;

import java.util.LinkedList;
import java.util.List;

public class ResultsetRowsConsuming extends AbstractResultsetRows implements ResultsetRows {
    /**
     * 用 LinkedList 方便移除头部
     */
    private LinkedList<Row> rows;

    public ResultsetRowsConsuming(List<? extends Row> rows, ColumnDefinition columnDefinition) {
        this.currentPositionInFetchedRows = -1; // JDBC 规范：初始位置在第一行之前
        this.rows = (LinkedList<Row>) rows;
        this.metadata = columnDefinition;
    }

    @Override
    public Row next() {
        this.currentPositionInFetchedRows++;
        System.out.println("【ResultsetRowsConsuming.next()】 currentPositionInFetchedRows: " + this.currentPositionInFetchedRows + ", row size: " + this.rows.size());
        if (rows.isEmpty()) {
            afterLast();
            return null;
        }
        // 每次取一行并删除，边消费边释放内存
        Row row = rows.removeFirst();
        return row.setMetadata(this.metadata);
    }

    @Override
    public boolean hasNext() {
        return !rows.isEmpty();
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public void beforeFirst() {
        throw new UnsupportedOperationException("Forward-only consuming ResultSet does not support beforeFirst()");
    }

    @Override
    public boolean isAfterLast() {
        return false;
    }

    @Override
    public boolean isBeforeFirst() {
        return false;
    }

    @Override
    public void afterLast() {
        this.currentPositionInFetchedRows = this.rows.size();
    }

    @Override
    public Row get(int atIndex) {
        throw new UnsupportedOperationException("Forward-only consuming ResultSet does not support random access");
    }
}

