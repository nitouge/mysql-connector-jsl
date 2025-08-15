package com.mysql.cj.lsj.demo.result;

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
 * 流式读取满足条件：
 * （1）MySQL特殊要求：必须设置fetch size为Integer.MIN_VALUE才是流式，useCursorFetch无需再设置
 * （2）resultSetType必须ResultSet.TYPE_FORWARD_ONLY（可以不设置）
 * （1）Statement和PreparedStatement都支持
 * （2）resultSetConcurrency可以为ResultSet.CONCUR_READ_ONLY（可以不设置，和resultSetType保持同步），不能设置为ResultSet.CONCUR_UPDATABLE
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
public class StreamFetchEnableDemo {

    final String sql = "select * from test.t_user";

    final int fetchSize = 3;
    /**
     * 流式读取，PreparedStatement + setFetchSize(Integer.MIN_VALUE)
     * 成功读取
     */
    @Test
    public void streamAndUseCursor() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 设置fetch size
            ps.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 流式读取，PreparedStatement + setFetchSize(Integer.MIN_VALUE)
     * 成功读取
     */
    @Test
    public void streamConfigAndUseCursor() throws Exception {
        try (Connection conn = JDBCUtils.getCursorFetchConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_UPDATABLE)) {

            // 设置fetch size
            ps.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 流式读取，PreparedStatement + setFetchSize(Integer.MIN_VALUE)
     * 成功读取
     */
    @Test
    public void streamByEnable() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 设置fetch size
            ps.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 流式读取，Statement + setFetchSize(Integer.MIN_VALUE)
     * 成功读取
     */
    @Test
    public void streamStatementByEnable() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement()) {

            // 设置fetch size
            st.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = st.executeQuery(sql)) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * 流式读取，未设置st.setFetchSize(Integer.MIN_VALUE)
     * 无法流式读取
     */
    @Test
    public void streamByDisable() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql
                     // , ResultSet.TYPE_FORWARD_ONLY
                     // , ResultSet.CONCUR_READ_ONLY
             )) {

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
    public void fetchByScrollAndEnableUseCursor() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * Fetch size必须是Integer.MIN_VALUE
     * ResultSetConcurrency: CONCUR_UPDATABLE 无法流式读取
     * ResultSetConcurrency: CONCUR_READ_ONLY 可以流式读取
     *
     * @throws Exception
     */
    @Test
    public void streamByPreparedStatement() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // 设置fetch size
            ps.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }

    /**
     * Fetch size必须是Integer.MIN_VALUE
     * ResultSetConcurrency: CONCUR_UPDATABLE 无法流式读取
     * ResultSetConcurrency: CONCUR_READ_ONLY 可以流式读取
     *
     * @throws Exception
     */
    @Test
    public void streamByStatement() throws Exception {
        try (Connection conn = JDBCUtils.getConnection();
             Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {

            // 设置fetch size
            st.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = st.executeQuery(sql)) {
                List<UserVO> users = MapUtils.mapResultSetToList(rs, UserVO.class);
                users.forEach(System.out::println);
            }
        }
    }


}
