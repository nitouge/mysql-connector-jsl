package com.mysql.cj.protocol.a.result;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.result.Row;

import java.util.List;

/**
 * 使用 CircularBuffer 替代 LinkedList，并按估算字节大小做动态伸缩。
 */
public class ResultsetCircularBufferRowsConsuming extends AbstractResultsetRows implements ResultsetRows {

    private CircularBuffer<ResultsetRow> buffer;

    private long estimatedBufferBytes = 0L; // 缓冲中数据的估算字节数（近似）

    private static final int DEFAULT_CAPACITY = 128;

    // 字节上下限（可按需调整）
    private static final long MAX_BUFFER_BYTES = 64L * 1024 * 1024; // 64MB

    private static final long MIN_BUFFER_BYTES = 8L * 1024 * 1024;  // 8MB

    public ResultsetCircularBufferRowsConsuming(List<ResultsetRow> rows, ColumnDefinition columnDefinition) {
        this.currentPositionInFetchedRows = -1;
        this.metadata = columnDefinition;

        int initCap = Math.max(DEFAULT_CAPACITY, rows.size());
        this.buffer = new CircularBuffer<>(initCap);

        // 将 rows 放入环形缓冲，并累计估算字节
        for (ResultsetRow row : rows) {
            // 为了能通过 row.getBytes / getNull 正确估算，先设置 metadata
            row.setMetadata(this.metadata);
            buffer.add(row);
            this.estimatedBufferBytes += estimateRowSize(row);
        }
    }

    public ResultsetCircularBufferRowsConsuming(CircularBuffer<ResultsetRow> buffer, ColumnDefinition columnDefinition) {
        this.currentPositionInFetchedRows = -1;
        this.metadata = columnDefinition;
        this.buffer = buffer;
    }

    @Override
    public Row next() {
        if (buffer.isEmpty()) {
            afterLast();
            return null;
        }

        Row row = buffer.removeFirst();

        // 在移除后更新估算字节数（构造时已估算并累计）
        int removedBytes = estimateRowSize(row);
        this.estimatedBufferBytes -= removedBytes;
        if (this.estimatedBufferBytes < 0) {
            this.estimatedBufferBytes = 0;
        }

        this.currentPositionInFetchedRows++;
        // 每次取出后尝试调整容量（逻辑尽量轻量）
        adjustCapacity();

        return row.setMetadata(this.metadata);
    }

    @Override
    public boolean hasNext() {
        return !buffer.isEmpty();
    }

    @Override
    public int size() {
        return buffer.size();
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
        this.currentPositionInFetchedRows = this.buffer.size();
    }

    @Override
    public Row get(int atIndex) {
        throw new UnsupportedOperationException("Forward-only consuming ResultSet does not support random access");
    }

    /**
     * 粗略估算一行数据占用的字节数（只用于容量调整，精确度不必严苛）。
     * - 优先使用 row.getBytes(i)（如果实现返回字节表示）；
     * - 如果为 null 且 row.getNull(i) 为 false，则用默认近似值（对象头/引用等）。
     * <p>
     * 注意：Row 接口没有 size()，列数从 metadata.getFields().length 获得。Row 的 getBytes/getNull 等方法在内部实现中存在。:contentReference[oaicite:3]{index=3}
     */
    private int estimateRowSize(Row row) {
        if (row == null) {
            return 0;
        }

        int sizeBytes = 0;

        // 用 metadata 得到列数（如果 metadata 可用）
        int colCount = 0;
        if (this.metadata != null) {
            try {
                // ColumnDefinition#getFields() 返回 Field[]（DefaultColumnDefinition 实现）
                Object[] fields = this.metadata.getFields();
                if (fields != null) {
                    colCount = fields.length;
                }
            } catch (Throwable t) {
                // 保底：如果 metadata/实现不可用，跳过并返回一个默认估算
                colCount = 0;
            }
        }

        if (colCount == 0) {
            // 无法得知列数时，返回一个保守默认值
            return 64;
        }

        for (int i = 0; i < colCount; i++) {
            try {
                byte[] bytes = row.getBytes(i); // Row 接口通常提供 getBytes(int)
                if (bytes != null) {
                    sizeBytes += bytes.length;
                } else if (row.getNull(i)) {
                    // null -> 0
                } else {
                    // 非字节表示（数字/时间/String 等），做个近似估算
                    sizeBytes += 24; // 一个保守的对象引用/头的估算
                }
            } catch (IndexOutOfBoundsException ex) {
                // row 实现可能采用不同的索引策略，遇到越界就跳出
                break;
            } catch (Throwable t) {
                // 任何异常都不要让估算失败
                sizeBytes += 16;
            }
        }

        return sizeBytes;
    }

    /**
     * 动态调整缓冲区容量（混合按元素数与按字节总量策略）。
     * 目标：
     * - 小容量时（capacity <= 10000）：优先按元素利用率（>75% 扩容）；
     * - 大容量时（>10000）：更保守地避免过度分配，若元素过少且字节占用小则缩容；
     * - 始终参考 estimatedBufferBytes 与 MAX/MIN 字节阈值进行按需调整。
     */
    private void adjustCapacity() {
        int curCap = buffer.capacity();
        int curSize = buffer.size();

        // 1) 元素数驱动的快速扩容/缩容（低成本判断）
        if (curSize > curCap * 0.75) {
            int newCap = Math.max(curCap + curCap / 2, curSize + 1); // 1.5x
            buffer.resizeTo(newCap);
            return;
        }

        if (curCap > 10000 && curSize < curCap / 4 && curCap > DEFAULT_CAPACITY) {
            int shrinkTo = Math.max(DEFAULT_CAPACITY, Math.max(curSize * 2, DEFAULT_CAPACITY));
            buffer.resizeTo(shrinkTo);
            return;
        }

        // 2) 基于字节的阈值策略（更细粒度）
        long estBytes = this.estimatedBufferBytes;

        if (estBytes > MAX_BUFFER_BYTES) {
            // 估算平均每行字节，计算一个按字节目标容量（保守）
            int avg = curSize > 0 ? (int) Math.max(1, estBytes / curSize) : 64;
            int desiredByBytes = Math.max(DEFAULT_CAPACITY, (int) (MAX_BUFFER_BYTES / avg));
            if (desiredByBytes > curCap) {
                int newCap = Math.max(curCap + curCap / 2, desiredByBytes);
                buffer.resizeTo(newCap);
            }
        } else if (estBytes < MIN_BUFFER_BYTES && curSize < curCap / 4 && curCap > DEFAULT_CAPACITY) {
            int shrinkTo = Math.max(DEFAULT_CAPACITY, Math.max(curSize * 2, DEFAULT_CAPACITY));
            buffer.resizeTo(shrinkTo);
        }
    }
}

