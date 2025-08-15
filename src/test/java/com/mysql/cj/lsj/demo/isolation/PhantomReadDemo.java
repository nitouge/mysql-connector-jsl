package com.mysql.cj.lsj.demo.isolation;

import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 幻读
 */
public class PhantomReadDemo {

    public static void main(String[] args) throws Exception {
        // 初始化表
        try (Connection conn = JDBCUtils.getConnection()) {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS products (id INT PRIMARY KEY, name VARCHAR(20))");
            conn.createStatement().execute("TRUNCATE TABLE products");
            conn.createStatement().execute("INSERT INTO products VALUES (1, 'Product A')");
        }

        // 事务A (REPEATABLE READ)
        Thread threadA = new Thread(() -> {
            try (Connection connA = JDBCUtils.getConnection()) {
                connA.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                connA.setAutoCommit(false);

                System.out.println("[事务A] 初始查询:");
                printResults(connA, "SELECT * FROM products"); // 只有id=1

                // 等待事务B插入数据
                Thread.sleep(2000);

                // 尝试更新（当前读操作）
                System.out.println("[事务A] 执行范围更新:");
                int updated = connA.createStatement().executeUpdate(
                        "UPDATE products SET name = CONCAT(name, '*') WHERE id > 0"
                );
                System.out.println("影响行数: " + updated); // 会更新到事务B插入的行！

                System.out.println("[事务A] 更新后查询:");
                printResults(connA, "SELECT * FROM products"); // 仍只能看到id=1?

                connA.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 事务B (插入新数据)
        Thread threadB = new Thread(() -> {
            try (Connection connB = JDBCUtils.getConnection()) {
                Thread.sleep(1000); // 确保事务A先执行查询
                connB.setAutoCommit(false);
                System.out.println("[事务B] 插入新数据...");
                connB.createStatement().executeUpdate("INSERT INTO products VALUES (2, 'Product B')");
                connB.commit();
                System.out.println("[事务B] 已提交");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
    }

    static void printResults(Connection conn, String sql) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            System.out.println("id: " + rs.getInt("id") + ", name: " + rs.getString("name"));
        }
    }
}
