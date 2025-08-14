package com.mysql.cj.lsj.demo;

import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

/**
 * 四种隔离级别详解 (注：MySQL的REPEATABLE READ通过间隙锁（Gap Lock）解决了幻读问题)
 * 隔离级别	          脏读    不可重复读    幻读    实现机制
 * READ UNCOMMITTED	  ✓	        ✓	      ✓	     无锁，直接读最新数据
 * READ COMMITTED	  ✗      	✓	      ✓	     读操作使用快照（提交后更新）
 * REPEATABLE READ	  ✗	        ✗	      ✓*	 事务内首次读建立快照
 * SERIALIZABLE	      ✗	        ✗	      ✗	     全表锁，禁止并发
 */
public class TransactionIsolationDemo {

    public static void main(String[] args) throws Exception {
        // 依次测试四个隔离级别
        int[] levels = {
                Connection.TRANSACTION_READ_UNCOMMITTED,
                Connection.TRANSACTION_READ_COMMITTED,
                Connection.TRANSACTION_REPEATABLE_READ,
                Connection.TRANSACTION_SERIALIZABLE
        };

        for (int level : levels) {
            System.err.println("\n=== 测试隔离级别: " + levelToString(level) + " ===");
            testIsolation(level);
        }
    }

    private static void testIsolation(int isolationLevel) throws Exception {
        // 先清理测试表
        try (Connection conn = JDBCUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS test.t_balance");
            stmt.executeUpdate("CREATE TABLE test.t_balance (id INT PRIMARY KEY, balance INT)");
            stmt.executeUpdate("INSERT INTO test.t_balance VALUES (1, 100)");
        }

        CountDownLatch latch = new CountDownLatch(1);

        Thread reader = new Thread(() -> {
            try (Connection conn = JDBCUtils.getConnection()) {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(isolationLevel);

                System.out.println("线程名：" + Thread.currentThread().getName() + "---[Reader] 初次读取...");
                try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM test.t_balance WHERE id=1")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("线程名：" + Thread.currentThread().getName() + "---[Reader] 第一次读到 balance = " + rs.getInt(1));
                    }
                }

                latch.countDown(); // 让写线程开始

                Thread.sleep(3000); // 等写线程提交后再读一次

                System.out.println("线程名：" + Thread.currentThread().getName() + "---[Reader] 第二次读取...");
                try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM test.t_balance WHERE id=1")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("线程名：" + Thread.currentThread().getName() + "---[Reader] 第二次读到 balance = " + rs.getInt(1));
                    }
                }

                conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "reader-thread");

        Thread writer = new Thread(() -> {
            try (Connection conn = JDBCUtils.getConnection()) {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(isolationLevel);

                latch.await(); // 等 reader 第一次读完

                System.out.println("线程名：" + Thread.currentThread().getName() + "---[Writer] 更新 balance = 200（未提交）");
                try (PreparedStatement ps = conn.prepareStatement("UPDATE test.t_balance SET balance = 200 WHERE id=1")) {
                    ps.executeUpdate();
                }

                Thread.sleep(2000); // 延迟提交

                conn.commit();
                System.out.println("线程名：" + Thread.currentThread().getName() + "---[Writer] 提交事务");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "writer-thread");

        reader.start();
        writer.start();

        reader.join();
        writer.join();
    }

    private static String levelToString(int level) {
        switch (level) {
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return "READ UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED:
                return "READ COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ:
                return "REPEATABLE READ";
            case Connection.TRANSACTION_SERIALIZABLE:
                return "SERIALIZABLE";
            default:
                return "UNKNOWN";
        }
    }
}
