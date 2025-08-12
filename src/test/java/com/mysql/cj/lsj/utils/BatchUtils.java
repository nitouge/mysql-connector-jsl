package com.mysql.cj.lsj.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

public class BatchUtils {

    private BatchUtils() {}

    public static void main(String[] args) throws Exception {

        // 插入5000条t_account数据
        // setupAccountTable(10);

        // 插入10万条t_big_data数据
        // setupBigDataTable(100_000);

        // 插入100万条t_big_data数据
        setupBigDataTable(1_000_000);
    }

    /**
     * 批量插入t_account表
     */
    public static void setupAccountTable(int count) throws Exception {
        String create = "CREATE TABLE IF NOT EXISTS test.t_account (\n" +
                "  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "  username VARCHAR(100),\n" +
                "  age INT,\n" +
                "  is_deleted TINYINT(1) DEFAULT 0\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(create);
            st.execute("TRUNCATE TABLE test.t_account");
        }

        String insertSql = "INSERT INTO test.t_account (username, age, is_deleted) VALUES (?, ?, ?)";

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "user_" + i);
                ps.setInt(2, 20 + (i % 30));
                ps.setInt(3, 0);
                ps.addBatch();

                if (i % 500 == 0) {
                    ps.executeBatch();
                    conn.commit();
                    System.out.println("Insert " + i + " rows into t_account");
                }
            }
            ps.executeBatch();
            conn.commit();
            System.out.println("Finished insert total " + count + " rows into t_account");
        }
    }

    /**
     * 创建t_big_data表并批量插入大量数据
     */
    public static void setupBigDataTable(int count) throws Exception {
        String create = "CREATE TABLE IF NOT EXISTS test.t_big_data (\n" +
                "  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "  col1 VARCHAR(50), col2 INT, col3 DOUBLE, col4 DATE, col5 TIMESTAMP,\n" +
                "  col6 VARCHAR(100), col7 INT, col8 DOUBLE, col9 DATE, col10 TIMESTAMP,\n" +
                "  col11 VARCHAR(50), col12 INT, col13 DOUBLE, col14 DATE, col15 TIMESTAMP\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(create);
            st.execute("TRUNCATE TABLE test.t_big_data");
        }

        String insertSql = "INSERT INTO test.t_big_data " +
                "(col1, col2, col3, col4, col5, col6, col7, col8, col9, col10, col11, col12, col13, col14, col15) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "str_" + i);
                ps.setInt(2, i % 1000);
                ps.setDouble(3, i * 1.11);
                ps.setDate(4, new Date(System.currentTimeMillis() - (i * 86400000L)));
                ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

                ps.setString(6, "str2_" + i);
                ps.setInt(7, (i + 50) % 500);
                ps.setDouble(8, i * 2.22);
                ps.setDate(9, new Date(System.currentTimeMillis() - (i * 43200000L)));
                ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));

                ps.setString(11, "str3_" + i);
                ps.setInt(12, i % 100);
                ps.setDouble(13, i * 3.33);
                ps.setDate(14, new Date(System.currentTimeMillis() - (i * 21600000L)));
                ps.setTimestamp(15, new Timestamp(System.currentTimeMillis()));

                ps.addBatch();

                if (i % 1000 == 0) {
                    ps.executeBatch();
                    conn.commit();
                    System.out.println("Insert " + i + " rows into t_big_data");
                }
            }
            ps.executeBatch();
            conn.commit();
            System.out.println("Finished insert total " + count + " rows into t_big_data");
        }
    }

}


