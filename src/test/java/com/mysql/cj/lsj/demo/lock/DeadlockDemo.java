package com.mysql.cj.lsj.demo.lock;

import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeadlockDemo {

    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(() -> runTransaction1(), "T1");
        Thread t2 = new Thread(() -> runTransaction2(), "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    private static void runTransaction1() {
        try (Connection conn = JDBCUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM t_balance WHERE id = 1 FOR UPDATE");
                 PreparedStatement ps2 = conn.prepareStatement("UPDATE t_balance SET balance = balance + 10 WHERE id = 2")) {

                System.out.println("T1 locking id=1");
                ps1.executeQuery();

                // 模拟处理延迟
                Thread.sleep(2000);

                System.out.println("T1 updating id=2 ...");
                ps2.executeUpdate();
                System.out.println("T1 updated");

                System.out.println("T1 committing ...");
                conn.commit();
                System.out.println("T1 committed");

            } catch (CJException e) {
                int errorCode = e.getVendorCode();
                String sqlState = e.getSQLState();
                System.out.println("T1 DEADLOCK detected, rollback, error code: " + errorCode + ", sql state: " + sqlState);
                conn.rollback();
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runTransaction2() {
        try (Connection conn = JDBCUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM t_balance WHERE id = 2 FOR UPDATE");
                 PreparedStatement ps2 = conn.prepareStatement("UPDATE t_balance SET balance = balance - 10 WHERE id = 1")) {

                System.out.println("T2 locking id=2");
                ps1.executeQuery();

                // 模拟处理延迟
                Thread.sleep(2000);

                System.out.println("T2 updating id=1 ...");
                ps2.executeUpdate();
                System.out.println("T2 updated id=1");

                System.out.println("T2 committing ...");
                conn.commit();
                System.out.println("T2 committed");

            } catch (CJException e) {
                int errorCode = e.getVendorCode();
                String sqlState = e.getSQLState();
                System.out.println("T2 DEADLOCK detected, rollback, error code: " + errorCode + ", sql state: " + sqlState);
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

