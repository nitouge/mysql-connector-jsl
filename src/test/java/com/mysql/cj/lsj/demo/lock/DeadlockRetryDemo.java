package com.mysql.cj.lsj.demo.lock;

import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.lsj.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeadlockRetryDemo {

    private static final int MAX_RETRIES = 3;

    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(() -> runTransactionWithRetry1(), "T1");
        Thread t2 = new Thread(() -> runTransactionWithRetry2(), "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    private static void runTransactionWithRetry1() {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;
            try (Connection conn = JDBCUtils.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM t_balance WHERE id = 1 FOR UPDATE");
                     PreparedStatement ps2 = conn.prepareStatement("UPDATE t_balance SET balance = balance + 10 WHERE id = 2")) {

                    System.out.println("T1 attempt " + attempt + " locking id=1");
                    ps1.executeQuery();

                    Thread.sleep(2000);

                    System.out.println("T1 attempt " + attempt + " updating id=2");
                    ps2.executeUpdate();

                    conn.commit();
                    System.out.println("T1 committed successfully on attempt " + attempt);
                    break; // 成功退出循环

                } catch (CJException e) {
                    if (e.getVendorCode() == 1213) { // 死锁
                        System.out.println("T1 DEADLOCK detected, rollback, retrying...");
                        conn.rollback();
                    } else {
                        e.printStackTrace();
                        conn.rollback();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void runTransactionWithRetry2() {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;
            try (Connection conn = JDBCUtils.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM t_balance WHERE id = 2 FOR UPDATE");
                     PreparedStatement ps2 = conn.prepareStatement("UPDATE t_balance SET balance = balance + 10 WHERE id = 1")) {

                    System.out.println("T2 attempt " + attempt + " locking id=2");
                    ps1.executeQuery();

                    Thread.sleep(2000);

                    System.out.println("T2 attempt " + attempt + " updating id=1");
                    ps2.executeUpdate();

                    conn.commit();
                    System.out.println("T2 committed successfully on attempt " + attempt);
                    break;

                } catch (CJException e) {
                    if (e.getVendorCode() == 1213) { // 死锁
                        System.out.println("T2 DEADLOCK detected, rollback, retrying...");
                        conn.rollback();
                    } else {
                        e.printStackTrace();
                        conn.rollback();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

