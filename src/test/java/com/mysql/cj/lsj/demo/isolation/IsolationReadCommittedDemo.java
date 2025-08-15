package com.mysql.cj.lsj.demo.isolation;

import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IsolationReadCommittedDemo {

    public static void main(String[] args) throws Exception {
        testIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    public static void testIsolation(int isolationLevel) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 线程1：事务读取
        executor.submit(() -> {
            try (Connection conn = JDBCUtils.getConnection()) {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(isolationLevel);
                System.out.println("T1 start transaction");

                // 第一次读取
                try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, age FROM t_account WHERE id = 1")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("T1 first read id=" + rs.getLong(1));
                        System.out.println("T1 first read username=" + rs.getString(2));
                        System.out.println("T1 first read age=" + rs.getInt(3));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                }

                Thread.sleep(5000); // 等待 T2 修改数据

                // 第二次读取
                try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, age FROM t_account WHERE id = 1")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("T1 second read id=" + rs.getLong(1));
                        System.out.println("T1 second read username=" + rs.getString(2));
                        System.out.println("T1 second read age=" + rs.getInt(3));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                }

                Thread.sleep(3000); // 等待 T2 回滚数据
                // 第三次读取
                try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, age FROM t_account WHERE id = 1")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("T1 third read id=" + rs.getLong(1));
                        System.out.println("T1 third read username=" + rs.getString(2));
                        System.out.println("T1 third read age=" + rs.getInt(3));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                }

                conn.commit();
                System.out.println("T1 commit");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 线程2：事务修改
        executor.submit(() -> {
            try {
                Thread.sleep(2000); // 等待 T1 先读取
                try (Connection conn = JDBCUtils.getConnection()) {
                    conn.setAutoCommit(false);
                    conn.setTransactionIsolation(isolationLevel);
                    System.out.println("T2 start transaction");

                    try (PreparedStatement ps = conn.prepareStatement("UPDATE t_account SET age = age + 1 WHERE id = 1")) {
                        ps.executeUpdate();
                        System.out.println("T2 updated age");
                    }
                    Thread.sleep(5000); // 测试READ COMMITTED 未提交，T1线程读取未提交数据
                    conn.commit();
                    System.out.println("T2 commit");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executor.shutdown();
    }
}