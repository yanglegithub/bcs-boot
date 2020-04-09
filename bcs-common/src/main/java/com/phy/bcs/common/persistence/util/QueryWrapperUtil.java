package com.phy.bcs.common.persistence.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.phy.bcs.common.persistence.QueryDetail;
import com.phy.bcs.common.util.PublicUtils;
import com.phy.bcs.common.util.QueryUtils;
import com.phy.bcs.common.util.domain.Order;
import com.phy.bcs.common.util.domain.QueryCondition;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * QueryWrapper条件转换工具
 *
 * @author lijie
 */

public class QueryWrapperUtil {

    private static Collection handlerQueryConditionCollectionValue(QueryCondition queryCondition) {
        Collection col = null;
        if (queryCondition.getValue() instanceof String) {
            String val = String.valueOf(queryCondition.getValue());
            col = val.contains(",") ? Lists.newArrayList(val.split(","))
                : Lists.newArrayList(val);
        }
        if (queryCondition.getValue() instanceof Collection) {
            col = (Collection) queryCondition.getValue();
        }
        return col;
    }

    private static String handlerQueryConditionLikeValue(QueryCondition queryCondition) {
        String val = (String) queryCondition.getValue();
        if ("MySQL".equalsIgnoreCase(SqlUtils.jdbcType)) {
            val = SqlUtils.escapeWildcardsForMySQL(val);
        }
        return /*!val.startsWith("%") && !val.toString().endsWith("%")
            ? PublicUtils.toAppendStr("%", val, "%") :*/ val;
    }

    /**
     * 比较条件转换
     */
    private static void convertCondition(QueryDetail queryDetail, QueryWrapper queryWrapper, QueryCondition queryCondition) {
        Object queryValue = QueryUtils.getQueryValue(queryCondition, null);

        String sqlColumn = queryDetail.getSqlColumnByProperty(queryCondition.getFieldName());
        if (PublicUtils.isNotEmpty(queryCondition.getOperate())) {
            switch (queryCondition.getOperate()) {
                case notIn:
                    queryWrapper.notIn(sqlColumn, handlerQueryConditionCollectionValue(queryCondition));
                    break;
                case in:
                    queryWrapper.in(sqlColumn, handlerQueryConditionCollectionValue(queryCondition));
                    break;
                case like:
                    queryWrapper.like(sqlColumn, handlerQueryConditionLikeValue(queryCondition));
                    break;
                case notLike:
                    queryWrapper.notLike(sqlColumn, handlerQueryConditionLikeValue(queryCondition));
                    break;
                case between:
                    queryWrapper.between(sqlColumn
                        , queryValue
                        , QueryUtils.getQueryValue(queryCondition, queryCondition.getEndValue()));
                    break;
                case isNull:
                    queryWrapper.isNull(sqlColumn);
                    break;
                case isNotNull:
                    queryWrapper.isNotNull(sqlColumn);
                    break;
                default:
                    queryWrapper.apply(PublicUtils.toAppendStr(
                        sqlColumn, " ",
                        queryCondition.getOperate().getOperator(), " {0} "
                    ), queryValue);
                    break;
            }
        } else {
            throw new IllegalArgumentException("查询操作符无效");
        }
    }

    /**
     * 转换条件规则
     */
    public static QueryWrapper convertQueryDetail(QueryDetail queryDetail) {
        QueryWrapper queryWrapper = queryDetail.getRealQueryWrapper();
        return QueryWrapperUtil.convertQueryDetail(queryDetail, queryWrapper);
    }

    public static QueryWrapper convertQueryDetail(QueryDetail queryDetail, QueryWrapper queryWrapper) {
        //and 条件转换
        List<QueryCondition> andQueryConditions = queryDetail.getAndQueryConditions();
        if (PublicUtils.isNotEmpty(andQueryConditions)) {
            Iterator<QueryCondition> andIterator = andQueryConditions.iterator();
            while (andIterator.hasNext()) {
                QueryCondition queryCondition = andIterator.next();
                convertCondition(queryDetail, queryWrapper, queryCondition);
                andIterator.remove();
            }
            queryDetail.setAndQueryConditions(andQueryConditions);
        }

        //TODO or条件转换


        //排序条件转换
        List<Order> orders = queryDetail.getOrders();
        if (PublicUtils.isNotEmpty(orders)) {
            Iterator<Order> orderIterator = orders.iterator();
            while (orderIterator.hasNext()) {
                Order order = orderIterator.next();
                String fieldName = queryDetail.getSqlColumnByProperty(order.getProperty());
                queryWrapper.orderBy(true, Order.Direction.asc.equals(order.getDirection()), fieldName);
                orderIterator.remove();
            }
        }
        return queryWrapper;
    }


}
