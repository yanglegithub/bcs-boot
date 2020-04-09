package com.phy.bcs.common.persistence.util;


import com.phy.bcs.common.persistence.QueryDetail;
import com.phy.bcs.common.util.domain.QueryCondition;

import java.util.List;

/**
 * 查询条件构造器工具
 *
 * @author lijie
 */
public class Querys {

    private Querys() {
    }

    public static <T> QueryDetail<T> createQuery(Class<T> entityClass, QueryCondition... queryConditions) {
        return new QueryDetail<T>(entityClass).and(queryConditions);
    }

    public static <T> QueryDetail<T> createQuery(Class<T> entityClass, String mainTableAlias, QueryCondition... queryConditions) {
        return new QueryDetail<T>(entityClass, mainTableAlias).and(queryConditions);
    }

    public static <T> QueryDetail<T> createQuery(Class<T> entityClass, List<QueryCondition> conditionList) {
        return new QueryDetail<T>(entityClass).andAll(conditionList);
    }

    public static <T> QueryDetail<T> createQuery(Class<T> entityClass, String mainTableAlias, List<QueryCondition> conditionList) {
        return new QueryDetail<T>(entityClass, mainTableAlias).andAll(conditionList);
    }

}
