package com.mysql.cj.lsj.demo.isolation;

import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IsolationRepeatableReadDemo {
    public static void main(String[] args) throws Exception {
        setupTable();

        testIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }

    public static void setupTable() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("UPDATE t_account SET age = 0 WHERE id = 1");
        }
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
                int age = 0;
                 try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, age FROM t_account WHERE id = 1")) {
                 // try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, age FROM t_account WHERE id = 1 for update")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("T1 second read id=" + rs.getLong(1));
                        System.out.println("T1 second read username=" + rs.getString(2));
                        age = rs.getInt(3) + 100;
                        System.out.println("T1 second read age=" + rs.getInt(3));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE t_account SET age = ? WHERE id = 1")) {
                    ps.setInt(1, age);
                    ps.executeUpdate();
                }

                // Thread.sleep(3000); // 等待 T2 commit数据
                // 第三次读取
                // try (PreparedStatement ps = conn.prepareStatement("UPDATE t_account SET age = age + 1 WHERE id = 1")) {
                //     ps.executeUpdate();
                // }

               /* try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, age FROM t_account WHERE id = 1")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("T1 third read id=" + rs.getLong(1));
                        System.out.println("T1 third read username=" + rs.getString(2));
                        System.out.println("T1 third read age=" + rs.getInt(3));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                }*/

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
                    // Thread.sleep(5000);
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