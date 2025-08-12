package com.mysql.cj.lsj.demo;

import com.mysql.cj.lsj.utils.JDBCUtils;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;

public class BatchInsertDemo {

    private static final int BATCH_ROWS = 100000;

    @Test
    public void createTable() throws Exception {
        String create = "CREATE TABLE IF NOT EXISTS test.t_batch_test (\n" +
                "  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "  username VARCHAR(100),\n" +
                "  age INT,\n" +
                "  is_deleted TINYINT(1) DEFAULT 0\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(create);
            st.execute("TRUNCATE TABLE test.t_batch_test");
        }
    }

    @Test
    public void runSingleInsert() throws Exception {
        System.out.println("\n======== 单条插入 ========");
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO t_batch_test(username, age) VALUES (?, ?)")) {
            conn.setAutoCommit(false);
            long start = System.currentTimeMillis();
            for (int i = 0; i < BATCH_ROWS; i++) {
                ps.setString(1, "name_" + i);
                ps.setInt(2, 40 + (new Random().nextInt(10)));
                ps.executeUpdate();
            }
            conn.commit();
            long end = System.currentTimeMillis();
            System.out.println("耗时: " + (end - start) + " ms");
        }
        System.out.println("======== 单条插入 ========");
    }

    @Test
    public void runBatchInsert() throws Exception {
        System.out.println("\n======== 普通批处理（未开启rewriteBatchedStatements） ========");
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO t_batch_test(username, age) VALUES (?, ?)")) {
            conn.setAutoCommit(false);
            long start = System.currentTimeMillis();
            for (int i = 0; i < BATCH_ROWS; i++) {
                ps.setString(1, "name_" + i);
                ps.setInt(2, 40 + (new Random().nextInt(10)));
                ps.addBatch();
                if (i % 1000 == 0) {
                    ps.executeBatch(); // 批量提交
                }
            }
            ps.executeBatch();
            conn.commit();
            long end = System.currentTimeMillis();
            System.out.println("耗时: " + (end - start) + " ms");
        }
        System.out.println("======== 普通批处理（未开启rewriteBatchedStatements） ========");
    }

    /**
     * 1. 10万数据，批次10000，耗时：754ms，查看底层协议包被拆包了，大概3600多
     * 2. 10万数据，批次5000，耗时：966ms，查看底层协议包被拆包了，大概3600多
     * 3. 10万数据，批次4000，耗时：705ms，查看底层协议包被拆包了，大概3600多
     * 4. 10万数据，批次3000，耗时：732ms，查看底层协议包
     * 4. 10万数据，批次2000，耗时：690ms，查看底层协议包
     * 6. 10万数据，批次1000，耗时：782ms，查看底层协议包
     * 7. 10万数据，批次500，耗时：857ms，查看底层协议包
     *
     *
     *
     * @throws Exception
     */
    @Test
    public void runBatchInsertRewriteBatchedStatements() throws Exception {
        System.out.println("\n======== 批处理（开启rewriteBatchedStatements=true） ========");
        try (Connection conn = JDBCUtils.getRewriteBatchedConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO t_batch_test(username, age) VALUES (?, ?)")) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < BATCH_ROWS; i++) {
                    ps.setString(1, "name_" + i);
                    ps.setInt(2, 40 + (new Random().nextInt(10)));
                    ps.addBatch();
                    if (i % 5000 == 0) {
                        ps.executeBatch(); // 批量提交
                    }
                }
                ps.executeBatch();
                conn.commit();
                long end = System.currentTimeMillis();
                System.out.println("耗时: " + (end - start) + " ms");
            }
        }
        System.out.println("======== 批处理（开启rewriteBatchedStatements=true） ========");
    }

    @Test
    public void runMultiQueriesInsert() throws Exception {
        System.out.println("\n======== 普通批处理（开启allowMultiQueries=true） ========");
        try (Connection conn = JDBCUtils.getMultiQueriesConnection();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            long start = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < BATCH_ROWS; i++) {
                sb.append("INSERT INTO t_batch_test(username, age) VALUES ('")
                        .append("name_").append(i).append("', ")
                        .append(40 + (new Random().nextInt(10)))
                        .append(");");

                // 控制批次大小
                if (i % 1000 == 0 && i != 0) {
                    stmt.execute(sb.toString());
                    sb.setLength(0); // 清空
                }
            }

            if (sb.length() > 0) {
                stmt.execute(sb.toString());
            }
            conn.commit();
            long end = System.currentTimeMillis();
            System.out.println("耗时: " + (end - start) + " ms");
        }
        System.out.println("======== 普通批处理（开启allowMultiQueries=true） ========");
    }

    @Test
    public void runBatchInsertTraceProtocol() throws Exception {
        System.out.println("\n======== 批处理（开启traceProtocol=true） ========");
        try (Connection conn = JDBCUtils.getTraceProtocolConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO t_batch_test(username, age) VALUES (?, ?)")) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < 1; i++) {
                    ps.setString(1, "name_" + i);
                    ps.setInt(2, 40 + (new Random().nextInt(10)));
                    ps.addBatch();
                    if (i % 50 == 0) {
                        ps.executeBatch(); // 批量提交
                    }
                }
                ps.executeBatch();
                conn.commit();
                long end = System.currentTimeMillis();
                System.out.println(">>> runBatchInsertTraceProtocol耗时: " + (end - start) + " ms");
            }
        }
        System.out.println("======== 批处理（开启traceProtocol=true） ========");
    }
}

