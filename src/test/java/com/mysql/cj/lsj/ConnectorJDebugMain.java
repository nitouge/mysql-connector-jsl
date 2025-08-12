package com.mysql.cj.lsj;

import com.mysql.cj.lsj.entity.User;
import com.mysql.cj.lsj.utils.JDBCUtils;

import java.beans.Customizer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConnectorJDebugMain {

    private static final String sql = "select * from test.t_user";
    // private static final String sql = "select * from test.t_user where id = '1'";
    // private static final String sql = "select count(*) from test.t_user";

    public static void main(String[] args) throws Exception {
        // normalQuery();
        // cursorQuery();
        // streamQuery();
        customForwardConsumingQuery();
    }

    private static void customForwardConsumingQuery() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getForwardConsumingConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            List<User> userList = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setNickname(rs.getString("nickname"));
                user.setPassword(rs.getString("password"));
                user.setCountry(rs.getString("country"));
                user.setAge(rs.getInt("age"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setIsDeleted(rs.getInt("is_deleted"));
                userList.add(user);
            }
            // userList = MapUtils.mapResultSetToList(rs, User.class);
            userList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(rs, stmt, conn);
        }
    }

    private static void streamQuery() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConnection();
            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // MySQL特殊要求：必须设置fetch size为Integer.MIN_VALUE才是流式
            ps.setFetchSize(Integer.MIN_VALUE);
            rs = ps.executeQuery();
            List<User> userList = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setNickname(rs.getString("nickname"));
                user.setPassword(rs.getString("password"));
                user.setCountry(rs.getString("country"));
                user.setAge(rs.getInt("age"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setIsDeleted(rs.getInt("is_deleted"));
                userList.add(user);
            }

            // userList = MapUtils.mapResultSetToList(rs, User.class);
            userList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(rs, ps, conn);
        }
    }

    private static void cursorQuery() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getCursorFetchConnection();
            ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // 设置fetch size
            ps.setFetchSize(3);
            rs = ps.executeQuery();
            List<User> userList = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setNickname(rs.getString("nickname"));
                user.setPassword(rs.getString("password"));
                user.setCountry(rs.getString("country"));
                user.setAge(rs.getInt("age"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setIsDeleted(rs.getInt("is_deleted"));
                userList.add(user);
            }

            // userList = MapUtils.mapResultSetToList(rs, User.class);
            userList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(rs, ps, conn);
        }
    }

    private static void normalQuery() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            List<User> userList = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setNickname(rs.getString("nickname"));
                user.setPassword(rs.getString("password"));
                user.setCountry(rs.getString("country"));
                user.setAge(rs.getInt("age"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setIsDeleted(rs.getInt("is_deleted"));
                userList.add(user);
            }
            // userList = MapUtils.mapResultSetToList(rs, User.class);
            userList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(rs, stmt, conn);
        }
    }


}
