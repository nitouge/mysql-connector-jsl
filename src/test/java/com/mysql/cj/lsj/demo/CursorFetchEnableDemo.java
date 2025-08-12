package com.mysql.cj.lsj.demo;

import com.mysql.cj.lsj.utils.JDBCUtils;
import com.mysql.cj.lsj.utils.MapUtils;
import com.mysql.cj.lsj.vo.UserVO;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;


/**
 * 游标读取满足条件：
 * （1）useCursorFetch必须设置为true
 * （2）resultSetType必须ResultSet.TYPE_FORWARD_ONLY（可以不设置）
 * （1）Statement和PreparedStatement都支持
 * （2）resultSetConcurrency可以为ResultSet.CONCUR_READ_ONLY或ResultSet.CONCUR_UPDATABLE（可以不设置，和resultSetType保持同步）
 * <p>
 * <p>
 * resultSetType– 结果集类型:
 * ResultSet.TYPE_FORWARD_ONLY
 * ResultSet.TYPE_SCROLL_INSENSITIVE
 * ResultSet.TYPE_SCROLL_SENSITIVE
 * <p>
 * resultSetConcurrency – 并发类型:
 * ResultSet.CONCUR_READ_ONLY
 * ResultSet.CONCUR_UPDATABLE
 */
public class CursorFetchEnableDemo {

    final String sql = "select * from test.t_user";

    final int fetchSize = 3;

    /**
     * 游标方式，设置url上参数useCursorFetch=true，按批次抓取fetchSize条，但是没有配置结果集
     */
    @Test
    public void fetchByEnableUseCursorNotConfigResultSet() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 设置fetch size
            ps.setFetchSize(fetchSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 游标方式，设置url上参数useCursorFetch=true，按批次抓取fetchSize条，但是没有配置结果集
     */
    @Test
    public void fetchStatementByEnableUseCursorNotConfigResultSet() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             Statement st = conn.createStatement()) {

            // 设置fetch size
            st.setFetchSize(7);
            try (ResultSet rs = st.executeQuery(sql)) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 游标方式，设置url上参数useCursorFetch=true，按批次抓取fetchSize条
     */
    @Test
    public void fetchByEnableUseCursor() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(fetchSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 用TYPE_SCROLL_INSENSITIVE，一次性把全部结果加载到内存，支持随机访问
     *
     * @throws Exception
     */
    @Test
    public void fetchScroll() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = st.executeQuery(sql)) {

            rs.last();          // 游标移动到最后一行
            System.out.println("总行数: " + rs.getRow());  // 获取当前行号，也就是总行数

            rs.first();         // 游标回到第一行
            System.out.println("第一行: " + rs.getInt("id") + ", " + rs.getString("username"));

            if (rs.absolute(5)) {   // 游标跳转到第100行，成功返回true，失败false
                System.out.println("第5行: " + rs.getInt("id") + ", " + rs.getString("username"));
            }
        }

        System.out.println("\n================================ fetchScroll test finish ================================");
    }

    /**
     * 用TYPE_SCROLL_INSENSITIVE，一次性把全部结果加载到内存，支持随机访问
     *
     * @throws Exception
     */
    @Test
    public void fetchByScrollAndEnableUseCursor() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(fetchSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 游标方式，设置url上参数useCursorFetch=false（默认），按批次抓取fetchSize条
     *
     * @throws Exception
     */
    @Test
    public void fetchByDisableUseCursor() throws Exception {
        // Thread.sleep(15000);
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(fetchSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
        // Thread.sleep(600000);
    }

    /**
     * 游标方式，Statement设置fetchSize, 设置url上参数useCursorFetch=true，按批次抓取fetchSize条
     *
     * @throws Exception
     */
    @Test
    public void fetchByStatementAndEnableUseCursor() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {

            // 设置fetch size
            st.setFetchSize(fetchSize);
            try (ResultSet rs = st.executeQuery(sql)) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }


}
