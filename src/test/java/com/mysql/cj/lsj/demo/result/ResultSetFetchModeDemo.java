package com.mysql.cj.lsj.demo.result;

import com.mysql.cj.lsj.entity.BigData;
import com.mysql.cj.lsj.utils.JDBCUtils;
import com.mysql.cj.lsj.utils.MapUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class ResultSetFetchModeDemo {

    public static void main(String[] args) throws Exception {
        String sql = "select * from test.t_big_data";

        Thread.sleep(5_000);

       /* System.out.println("=== 游标方式（全部加载到内存54MB） ===");
        fetchCursor(sql);

        Thread.sleep(30_000);
        // 主动建议进行垃圾回收（注意这只是建议，JVM不保证立即执行）
        System.gc();
        Thread.sleep(30_000);*/

        // System.out.println("\n=== 未设置useCursorFetch=true游标方式===");
        // fetchCursorEnable(sql, 1000);
        //
        // Thread.sleep(30_000);
        // // 主动建议进行垃圾回收（注意这只是建议，JVM不保证立即执行）
        // System.gc();
        // Thread.sleep(30_000);

        System.out.println("\n=== 设置forwardConsuming=true方式 ===");
        fetchForwardConsuming(sql);

       /* System.out.println("\n=== 游标方式：1000行一次===");
        fetchCursorWithSize(sql, 1000);*/

       /* Thread.sleep(30_000);
        // 主动建议进行垃圾回收（注意这只是建议，JVM不保证立即执行）
        System.gc();
        Thread.sleep(30_000);

        System.out.println("\n=== 流式方式（边取边用） ===");
        fetchStreaming(sql);*/

        Thread.sleep(10 * 60_000);  // 10分钟保持JVM不退出
        System.out.println("主程序结束");
    }

    /**
     * 一边读取一边释放，减少内存占用
     * 设置forwardConsuming=true
     *
     * @param sql
     * @throws Exception
     */
    private static void fetchForwardConsuming(String sql) throws Exception {
        long start = System.currentTimeMillis();
        try (Connection conn = JDBCUtils.getForwardConsumingConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            try (ResultSet rs = ps.executeQuery()) {
                // int count = 0;
                // while (rs.next()) {
                //     count++;
                // }
                // System.out.println("总记录数: " + count);

                List<BigData> List = MapUtils.mapResultSetToList(rs, BigData.class);
                System.out.println("总记录数: " + List.size());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时(ms): " + (end - start));
    }

    /**
     * 用TYPE_SCROLL_INSENSITIVE，一次性把全部结果加载到内存，支持随机访问
     *
     * @param sql
     * @throws Exception
     */
    private static void fetch(String sql) throws Exception {
        long start = System.currentTimeMillis();
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = st.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
            }
            System.out.println("总记录数: " + count);
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时(ms): " + (end - start));
    }

    /**
     * 游标方式，设置setFetchSize按批次抓取（游标滚动，分批拉取）
     * 未设置useCursorFetch=true
     *
     * @param sql
     * @param fetchSize
     * @throws Exception
     */
    private static void fetchCursorEnable(String sql, int fetchSize) throws Exception {
        long start = System.currentTimeMillis();
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(fetchSize);
            try (ResultSet rs = ps.executeQuery()) {
                // int count = 0;
                // while (rs.next()) {
                //     count++;
                // }
                // System.out.println("总记录数: " + count);

                List<BigData> List = MapUtils.mapResultSetToList(rs, BigData.class);
                System.out.println("总记录数: " + List.size());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时(ms): " + (end - start));
    }

    /**
     * 游标方式，设置setFetchSize按批次抓取（游标滚动，分批拉取）
     * 设置useCursorFetch=true
     *
     * @param sql
     * @param fetchSize
     * @throws Exception
     */
    private static void fetchCursorWithSize(String sql, int fetchSize) throws Exception {
        long start = System.currentTimeMillis();
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(fetchSize);
            try (ResultSet rs = ps.executeQuery()) {
                // int count = 0;
                // while (rs.next()) {
                //     count++;
                // }
                // System.out.println("总记录数: " + count);

                List<BigData> List = MapUtils.mapResultSetToList(rs, BigData.class);
                System.out.println("总记录数: " + List.size());

            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时(ms): " + (end - start));
    }

    /**
     * MySQL流式读取，TYPE_FORWARD_ONLY + setFetchSize(Integer.MIN_VALUE)，一行行读取，内存最小
     *
     * @param sql
     * @throws Exception
     */
    private static void fetchStreaming(String sql) throws Exception {
        long start = System.currentTimeMillis();
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // MySQL特殊要求：必须设置fetch size为Integer.MIN_VALUE才是流式
            ps.setFetchSize(Integer.MIN_VALUE);

            try (ResultSet rs = ps.executeQuery()) {
                // int count = 0;
                // while (rs.next()) {
                //     count++;
                // }
                // System.out.println("总记录数: " + count);

                List<BigData> List = MapUtils.mapResultSetToList(rs, BigData.class);
                System.out.println("总记录数: " + List.size());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时(ms): " + (end - start));
    }
}
