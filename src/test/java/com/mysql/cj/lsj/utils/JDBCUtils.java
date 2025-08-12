package com.mysql.cj.lsj.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtils {

    private static final String URL = "jdbc:mysql://localhost:3306/test?" +
            "&characterEncoding=UTF-8" +
            "&autoReconnect=true" +
            "&useSSL=false" +
            "&connectTimeout=600000" +      // 连接超时600秒
            "&socketTimeout=600000" +       // 读写超时600秒
            "&serverTimezone=Asia/Shanghai";

    private static final String URL_CURSOR = "jdbc:mysql://localhost:3306/test?" +
            "useCursorFetch=true" +
            "&characterEncoding=UTF-8" +
            "&autoReconnect=true" +
            "&useSSL=false" +
            "&connectTimeout=600000" +      // 连接超时600秒
            "&socketTimeout=600000" +       // 读写超时600秒
            "&serverTimezone=GMT%2B8";

    private static final String URL_REWRITE = "jdbc:mysql://localhost:3306/test?" +
            "&characterEncoding=UTF-8" +
            "&autoReconnect=true" +
            "&rewriteBatchedStatements=true" +
            // "&maxRewriteBatchSize=5000" +
            "&useSSL=false" +
            "&connectTimeout=600000" +      // 连接超时600秒
            "&socketTimeout=600000" +       // 读写超时600秒
            "&serverTimezone=Asia/Shanghai";

    private static final String URL_MULTI = "jdbc:mysql://localhost:3306/test?" +
            "&characterEncoding=UTF-8" +
            "&autoReconnect=true" +
            "&allowMultiQueries=true" +
            // "&rewriteBatchedStatements=true" +
            "&useSSL=false" +
            "&connectTimeout=600000" +      // 连接超时600秒
            "&socketTimeout=600000" +       // 读写超时600秒
            "&serverTimezone=Asia/Shanghai";

    private static final String URL_TRACE_PROTOCOL = "jdbc:mysql://localhost:3306/test?" +
            "&characterEncoding=UTF-8" +
            "&autoReconnect=true" +
            "&useReadAheadInput=false" +
            "&traceProtocol=true" +
            "&rewriteBatchedStatements=true" +
            "&useSSL=false" +
            "&connectTimeout=600000" +      // 连接超时600秒
            "&socketTimeout=600000" +       // 读写超时600秒
            "&serverTimezone=Asia/Shanghai";

    private static final String URL_FORWARD_CONSUMING = "jdbc:mysql://localhost:3306/test?" +
            "&characterEncoding=UTF-8" +
            "&forwardConsuming=true" +
            "&autoReconnect=true" +
            "&useReadAheadInput=false" +
            "&traceProtocol=false" +
            "&rewriteBatchedStatements=true" +
            "&useSSL=false" +
            "&connectTimeout=600000" +      // 连接超时600秒
            "&socketTimeout=600000" +       // 读写超时600秒
            "&serverTimezone=Asia/Shanghai";

    private static final String USER = "root";

    private static final String PASSWORD = "root@123456";

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private JDBCUtils() {}

    /**
     * 获取连接
     *
     * @return Connection
     */
    public static Connection getConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    public static Connection getCursorFetchConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL_CURSOR, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    public static Connection getRewriteBatchedConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL_REWRITE, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    public static Connection getMultiQueriesConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL_MULTI, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    public static Connection getTraceProtocolConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL_TRACE_PROTOCOL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    public static Connection getForwardConsumingConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL_FORWARD_CONSUMING, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 释放资源, DML的执行不产生ResultSet结果集，可以传入一个null,因此要先判断传入的对象是否为空，若非空则调用close方法关闭资源（动态绑定）
     *
     * @param resultSet  : 结果集（DQL产生）
     * @param statement  : Statement接口或PreparedStatement接口的实现类
     * @param connection : 即通过getConnection方法获取到的连接
     */
    public static void close(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
