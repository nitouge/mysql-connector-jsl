package com.mysql.cj.lsj.demo;

import com.mysql.cj.lsj.utils.JDBCUtils;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

public class CRUDDemo {


    // public static void main(String[] args) throws Exception {
    //     insertAccount(5);
    //
    //     insertAccount("", 20 + (i % 10));
    //
    //     updateAccount(1, "Tom_Updated");
    //
    //     deleteAccount(2);
    //
    //     System.out.println("===== 流式查询测试（Streaming Mode） =====");
    //     streamQueryAccount();
    //
    //     System.out.println("===== 普通查询测试（全量加载） =====");
    //     normalQueryAccount();
    // }

    /**
     * 插入测试数据
     */
    @Test
    public void batchInsertAccount() throws Exception {
        String sql = "INSERT INTO t_account (username, age) VALUES (?, ?)";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 10; i++) {
                ps.setString(1, "User_" + i);
                ps.setInt(2, 20 + (i % 10));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * 插入测试数据
     */
    @Test
    public void insertAccount() throws Exception {
        String sql = "INSERT INTO t_account (username, age) VALUES (?, ?)";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "User_" + UUID.randomUUID());
            ps.setInt(2, 20 + (new Random().nextInt(20)));
            boolean execute = ps.execute();
            System.out.println(execute);
        }
    }

    /**
     * 更新
     */
    @Test
    public void updateAccount() throws Exception {
        String sql = "UPDATE t_account SET username = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "User_xxx");
            ps.setLong(2, 1);
            ps.executeUpdate();
        }
    }

    /**
     * 删除
     */
    @Test
    public void deleteAccount() throws Exception {
        String sql = "DELETE FROM t_account WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, 2);
            ps.executeUpdate();
        }
    }

    /**
     * 普通查询（一次性加载全部）
     */
    @Test
    public void normalQueryAccount() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT sleep(300)")) {
             // ResultSet rs = st.executeQuery("SELECT * FROM t_account")) {

            // while (rs.next()) {
            //     System.out.println(rs.getInt("id") + " - " + rs.getString("username"));
            // }
        }
    }

    /**
     * 流式查询（逐行读取）
     */
    @Test
    public void streamQueryAccount() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            // 触发流式模式
            st.setFetchSize(Integer.MIN_VALUE);

            try (ResultSet rs = st.executeQuery("SELECT * FROM t_account")) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " - " + rs.getString("username"));
                    Thread.sleep(200); // 模拟慢处理，方便抓包和断点调试
                }
            }
        }
    }

    /**
     * 一边读取一边释放，减少内存占用
     */
    @Test
    public void forwardConsumingQueryAccount() throws Exception {
        try (Connection conn = JDBCUtils.getForwardConsumingConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM t_account")) {

            while (rs.next()) {
                System.out.println(rs.getInt("id") + " - " + rs.getString("username"));
            }
        }
    }
}

