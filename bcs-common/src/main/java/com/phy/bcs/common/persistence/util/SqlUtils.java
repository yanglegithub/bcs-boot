package com.phy.bcs.common.persistence.util;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.phy.bcs.common.util.PublicUtils;
import com.phy.bcs.common.util.config.SystemConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "jdbc")
public class SqlUtils {

    public static String jdbcType;
    private static String identifierQuote;

    /**
     * 根据数据表实体类，获取属性对应的sql字段
     *
     * @param clazz        实体类，//一般指有@TableName注解 并且 extends GeneralEntity
     * @param propertyName 属性名称，如：name,status
     * @return fieldsSql 属性对应的sql字段
     */
    public static <T> String getSqlColumnByProperty(Class<T> clazz,
                                                    String propertyName) {
        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(clazz);
        if (PublicUtils.isNotEmpty(columnMap)) {
            ColumnCache columnCache = columnMap.get(propertyName.toUpperCase(Locale.ENGLISH));
            if (columnCache != null) {
                return columnCache.getColumn();
            }
        }
        return propertyName;
    }

    /**
     * 解决数据库保留字段未转换引号问题
     *
     * @Deprecated 各数据库保留字段不一，不再转换，请在TableFiled字段中用(反)引号包含内容
     */
    @Deprecated
    public static String sqlWordConvert(String convertStr) {
        return convertStr;
    }

    /**
     * 使用配置判断引号类型
     */
    private static String convertQuote(String column) {
        if (null != identifierQuote) {
            return String.format(identifierQuote, column);
        }
        return column;
    }

    private static String escapeStringForMySQL(String s) {
        return s.replaceAll("\\\\", "\\\\\\\\")
            .replaceAll("\b", "\\b")
            .replaceAll("\n", "\\n")
            .replaceAll("\r", "\\r")
            .replaceAll("\t", "\\t")
            .replaceAll("\\x1A", "\\Z")
            .replaceAll("\\x00", "\\0")
            .replaceAll("'", "\\\\'")
            .replaceAll("\"", "\\\\\"");
    }

    public static String escapeWildcardsForMySQL(String s) {
        return escapeStringForMySQL(s)
            .replaceAll("%", "\\\\%")
            .replaceAll("_", "\\\\_");
    }

    @Value("${jdbc.type:#{null}}")
    public void setJdbcType(String jdbcType) {
        SqlUtils.jdbcType = jdbcType;
        if ("mysql".equalsIgnoreCase(jdbcType)) {
            SqlUtils.identifierQuote = SystemConfig.get("jdbc.mysql.identifier.quote");
        } else if ("oracle".equalsIgnoreCase(jdbcType)) {
            SqlUtils.identifierQuote = SystemConfig.get("jdbc.oracle.identifier.quote");
        }
    }

}
