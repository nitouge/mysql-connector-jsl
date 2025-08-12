package com.mysql.cj.lsj.utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class MapUtils {

    private MapUtils() {}

    /**
     * 结果集映射实体
     *
     * @param rs
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> mapResultSetToList(ResultSet rs, Class<T> clazz) throws Exception {
        List<T> resultList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i); // 使用getColumnLabel支持SQL中的别名
                String fieldName = toCamelCase(columnName); // 转驼峰
                Object value = rs.getObject(i);
                // tinyint(1)转Integer
                if (value instanceof Boolean) {
                    value = ((Boolean) value) ? 1 : 0;
                }

                Field field;
                try {
                    field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(obj, value);
                } catch (Exception e) {
                    // System.err.println("数据库字段：" + columnName + ", 转为实体字段：" + fieldName);
                    continue; // 当前列在实体类中没有对应字段，跳过
                }
            }
            // Thread.sleep(1);
            resultList.add(obj);
        }
        return resultList;
    }

    /**
     * 下划线命名转换成Java驼峰命名
     *
     * @param columnName
     * @return
     */
    private static String toCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }


}
