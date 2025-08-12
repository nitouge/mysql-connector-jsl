package com.mysql.cj.lsj.demo;

import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * JDBC ResultSet behaviour demo:
 * - scroll insensitive vs scroll sensitive
 * - forward only
 * - updatable ResultSet (updateXXX + updateRow)
 * <p>
 * Adjust getConnection() to your environment.
 */
public class JDBCResultSetDemo {

    public static void main(String[] args) throws Exception {
        // 1) 准备表和数据
        setupTable();

        // 2) 运行测试
        System.out.println("\n*** Running forward-only test (prints rows while concurrent update happens) ***");
        testForwardOnly();

        // System.out.println("\n*** Running scroll-insensitive test ***");
        // testScrollInsensitive();
        //
        // System.out.println("\n*** Running scroll-sensitive test ***");
        // testScrollSensitive();
        //
        // System.out.println("\n*** Running updatable ResultSet test ***");
        // testUpdatableResultSet();

        System.out.println("\nDemo finished.");
    }

    /**
     * 建表并插入初始数据（方便反复测试）
     */
    public static void setupTable() throws Exception {
        String create = "CREATE TABLE IF NOT EXISTS t_user_demo (\n" +
                "  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "  username VARCHAR(100),\n" +
                "  age INT,\n                " +
                "  is_deleted TINYINT(1) DEFAULT 0\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        String truncate = "TRUNCATE TABLE t_user_demo;";
        String insert1 = "INSERT INTO t_user_demo (username, age, is_deleted) VALUES ('Tom', 20, 0);";
        String insert2 = "INSERT INTO t_user_demo (username, age, is_deleted) VALUES ('Lucy', 30, 0);";

        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(create);
            st.execute(truncate);
            st.execute(insert1);
            st.execute(insert2);
            System.out.println("Table prepared with sample data.");
        }
    }

    /**
     * 模拟并发更新：在另一个连接里等待delay秒后更新第1条记录的 username -> newName
     *
     */
    private static void spawnConcurrentUpdate(long delaySeconds, String newName) {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(delaySeconds);
                try (Connection c = JDBCUtils.getConnection();
                     PreparedStatement ps = c.prepareStatement("UPDATE t_user_demo SET username = ? WHERE id = 1")) {
                    ps.setString(1, newName);
                    int updated = ps.executeUpdate();
                    System.out.println("[ConcurrentUpdate] updated rows = " + updated + ", set username=" + newName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "Concurrent-Update-Thread").start();
    }

    /**
     * 测试TYPE_FORWARD_ONLY（通常不可滚动）
     * 主线程逐行读取，每读一行等待 1 秒；并发线程在第2秒更新id=1的username。
     * 你会看到读取到id=1时通常是初始值（如果并发更新发生在读取之前，会读到新值；时间竞态决定），但forward-only本质上只是“读取流”
     * <p>
     * [Forward] id=1, username=Tom
     * [Forward] id=2, username=Lucy
     * Demo finished.
     * [ConcurrentUpdate] updated rows = 1, set username=Tom_forward
     */
    public static void testForwardOnly() throws Exception {
        System.out.println("=== testForwardOnly ===");
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = st.executeQuery("SELECT id, username FROM t_user_demo ORDER BY id")) {

            // 启动并发修改：2 秒后把 id=1 的 username 改为 "alice_forward"
            spawnConcurrentUpdate(2, "Tom_forward");

            // 逐行读取并打印
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                System.out.println("[Forward] id=" + id + ", username=" + username);

                // sleep 1s between rows to allow concurrent update to happen while reading
                TimeUnit.SECONDS.sleep(1);
            }
        }
    }

    /**
     * 测试TYPE_SCROLL_INSENSITIVE（不敏感）：通常不会反映并发修改
     * 通常会缓存结果集快照，不会反映并发修改，所以 before 和 after 很可能相同。refreshRow() 可能无效或抛异常，依驱动而定
     * <p>
     * [insensitive.before] id=1, username=Tom
     * [ConcurrentUpdate] updated rows = 1, set username=Tom_insensitive
     * [insensitive.after] id=1, username=Tom
     * [insensitive.refresh] refreshRow() not supported or no effect: Result Set not updatable.
     * This result set must come from a statement that was created with a result set type of ResultSet.CONCUR_UPDATABLE,
     * the query must select only one table, can not use functions and must select all primary keys from that table.
     * See the JDBC 2.1 API Specification, section 5.6 for more details.
     */
    public static void testScrollInsensitive() throws Exception {
        System.out.println("=== testScrollInsensitive ===");
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = st.executeQuery("SELECT id, username FROM t_user_demo ORDER BY id")) {

            spawnConcurrentUpdate(2, "Tom_insensitive");

            // 先移动到第一行并读取
            if (rs.first()) {
                System.out.println("[insensitive.before] id=" + rs.getInt("id") + ", username=" + rs.getString("username"));
            }

            // wait to let concurrent update happen
            TimeUnit.SECONDS.sleep(3);

            // 再次读取当前行：对于INSENSITIVE，通常仍然是旧值（驱动依赖）
            if (rs.first()) {
                System.out.println("[insensitive.after] id=" + rs.getInt("id") + ", username=" + rs.getString("username"));
            }

            // 也可以尝试rs.refreshRow()看驱动是否支持刷新
            try {
                rs.refreshRow();
                System.out.println("[insensitive.refresh] id=" + rs.getInt("id") + ", username=" + rs.getString("username"));
            } catch (SQLException e) {
                System.out.println("[insensitive.refresh] refreshRow() not supported or no effect: " + e.getMessage());
            }
        }
    }

    /**
     * 测试TYPE_SCROLL_SENSITIVE（敏感） - 驱动可能不真正支持敏感，此测试用于观察差异
     * 理论上会反映数据库更改，但很多驱动并不完全实现“敏感”语义；如果驱动支持，你会看到after读到更新后的值。实际结果可能与INSENSITIVE相同
     * <p>
     * [sensitive.before] id=1, username=Tom
     * [ConcurrentUpdate] updated rows = 1, set username=Tom_sensitive
     * [sensitive.after] id=1, username=Tom
     * [sensitive.refresh] refreshRow() not supported or no effect: Result Set not updatable.
     * This result set must come from a statement that was created with a result set type of ResultSet.CONCUR_UPDATABLE,
     * the query must select only one table, can not use functions and must select all primary keys from that table.
     * See the JDBC 2.1 API Specification, section 5.6 for more details.
     */
    public static void testScrollSensitive() throws Exception {
        System.out.println("=== testScrollSensitive ===");
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = st.executeQuery("SELECT id, username FROM t_user_demo ORDER BY id")) {

            spawnConcurrentUpdate(2, "Tom_sensitive");

            if (rs.first()) {
                System.out.println("[sensitive.before] id=" + rs.getInt("id") + ", username=" + rs.getString("username"));
            }

            // 等待并发更新
            TimeUnit.SECONDS.sleep(3);

            // 有些驱动会在这里反映更新
            if (rs.first()) {
                System.out.println("[sensitive.after] id=" + rs.getInt("id") + ", username=" + rs.getString("username"));
            }

            // 也尝试refreshRow()
            try {
                rs.refreshRow();
                System.out.println("[sensitive.refresh] id=" + rs.getInt("id") + ", username=" + rs.getString("username"));
            } catch (SQLException e) {
                System.out.println("[sensitive.refresh] refreshRow() not supported or no effect: " + e.getMessage());
            }
        }
    }

    /**
     * 演示CONCUR_UPDATABLE，使用rs.updateXXX + rs.updateRow()
     * 注意：并非所有驱动都支持，通常要是单表查询并包含主键列，不能带复杂的JOIN/聚合/子查询等。
     * <p>
     * [updatable.before] id=1, username=Tom, age=20
     * [updatable.after] updated rs.updateRow()
     * [updatable.dbcheck] id=1, username=Tom_updated_rs, age=21
     */
    public static void testUpdatableResultSet() throws Exception {
        System.out.println("=== testUpdatableResultSet ===");
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = st.executeQuery("SELECT id, username, age FROM t_user_demo ORDER BY id")) {

            // 移动到第一行
            if (rs.first()) {
                System.out.println("[updatable.before] id=" + rs.getInt("id") + ", username=" + rs.getString("username") + ", age=" + rs.getInt("age"));

                // 更新当前行（仅更新ResultSet缓存）
                rs.updateString("username", "Tom_updated_rs");
                rs.updateInt("age", rs.getInt("age") + 1);

                // 写回数据库
                rs.updateRow();
                System.out.println("[updatable.after] updated rs.updateRow()");
            }

            // 验证数据库中的真实值（新连接查询）
            try (Connection c2 = JDBCUtils.getConnection();
                 PreparedStatement ps = c2.prepareStatement("SELECT id, username, age FROM t_user_demo WHERE id = 1")) {
                try (ResultSet rs2 = ps.executeQuery()) {
                    if (rs2.next()) {
                        System.out.println("[updatable.dbcheck] id=" + rs2.getInt("id") + ", username=" + rs2.getString("username") + ", age=" + rs2.getInt("age"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Note: CONCUR_UPDATABLE/ updateRow() may not be supported by your DB/driver for this query.");
        }
    }
}
