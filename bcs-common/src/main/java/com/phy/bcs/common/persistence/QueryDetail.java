package com.phy.bcs.common.persistence;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.phy.bcs.common.persistence.util.QueryWrapperUtil;
import com.phy.bcs.common.util.PublicUtils;
import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.common.util.domain.Order;
import com.phy.bcs.common.util.domain.PageModel;
import com.phy.bcs.common.util.domain.QueryCondition;
import com.phy.bcs.common.util.exception.RuntimeMsgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * 查询条件构造器
 *
 * @author lijie
 */
public class QueryDetail<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SELF_ENTITY_ALIAS = "a";
    protected static Logger logger = LoggerFactory.getLogger(QueryDetail.class);
    public Class<T> entityClass;

    private String mainTableAlias;

    private Map<String, Map<String, ColumnCache>> entityColumnMap = Maps.newHashMap();

    private QueryWrapper queryWrapper = new QueryWrapper<T>();

    /**
     * and条件
     */
    private List<QueryCondition> andQueryConditions = Lists.newArrayList();

    /**
     * or条件
     */
    private List<QueryCondition> orQueryConditions = Lists.newArrayList();

    /**
     * 排序属性
     */
    private List<Order> orders = Lists.newArrayList();

    public QueryDetail(Class<T> entityClass) {
        this(entityClass, null, null);
    }

    public QueryDetail(Class<T> entityClass, String mainTableAlias) {
        this(entityClass, mainTableAlias, null);
    }

    public QueryDetail(Class<T> entityClass, String mainTableAlias, Wrapper wrapper) {
        this.entityClass = entityClass;
        this.mainTableAlias = mainTableAlias;
        if (wrapper != null) {
            this.queryWrapper = (QueryWrapper) wrapper;
        }
        getCheckEntityClass();
        initColumnMap();
    }

    protected Class<T> getCheckEntityClass() {
        Assert.notNull(this.entityClass,
                "entityClass must not null,please set entity before use this method!", new Object[0]);
        return this.entityClass;
    }

    private void initColumnMap() {
        addAliasJoinEntity(SELF_ENTITY_ALIAS, this.entityClass);
        if (StringUtils.isNotEmpty(this.mainTableAlias) && !SELF_ENTITY_ALIAS.equals(this.mainTableAlias)) {
            addAliasJoinEntity(this.mainTableAlias, this.entityClass);
        }
    }

    public QueryDetail<T> addAliasJoinEntity(String alias, Class<?> joinEntity) {
        if (joinEntity != null) {
            Map<String, ColumnCache> columnMap = LambdaUtils
                    .getColumnMap(joinEntity);
            if (PublicUtils.isNotEmpty(columnMap)) {
                entityColumnMap.put(alias, columnMap);
            }
        }
        return this;
    }

    /**
     * 根据属性名称获取对应的sql字段
     */
    public String getSqlColumnByProperty(String propertyName) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new RuntimeMsgException("exception.common.query.paramNameNull");
        }

        String[] a = propertyName.split("\\.");
        if (a.length == 1) {
            return getSqlColumnByProperty(SELF_ENTITY_ALIAS, propertyName);
        } else if (a.length == 2) {
            return getSqlColumnByProperty(a[0], a[1]);
        } else {
            throw new RuntimeMsgException("exception.common.query.paramNameFormatError");
        }
    }

    private String getSqlColumnByProperty(String alias, String propertyName) {
        if (PublicUtils.isEmpty(this.entityColumnMap)) {
            return propertyName;
        }

        Map<String, ColumnCache> cacheMap = this.entityColumnMap.get(alias);
        if (cacheMap == null) {
            return alias + "." + propertyName;
        }
        ColumnCache columnCache = cacheMap.get(propertyName.toUpperCase(Locale.ENGLISH));
        if (columnCache != null) {
            if (SELF_ENTITY_ALIAS.equals(alias) && mainTableAlias == null) {
                return columnCache.getColumn();
            } else {
                return alias + "." + columnCache.getColumn();
            }
        }
        return alias + "." + propertyName;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public QueryDetail<T> setOrders(List<Order> orders) {
        this.orders = orders;
        return this;
    }

    public List<QueryCondition> getAndQueryConditions() {
        return andQueryConditions;
    }

    public void setAndQueryConditions(List<QueryCondition> andQueryConditions) {
        this.andQueryConditions = andQueryConditions;
    }

    public List<QueryCondition> getOrQueryConditions() {
        return orQueryConditions;
    }

    public void setOrQueryConditions(List<QueryCondition> orQueryConditions) {
        this.orQueryConditions = orQueryConditions;
    }

    /**
     * 添加一个and条件
     *
     * @param condition 该条件
     * @return 链式调用
     */
    public QueryDetail<T> and(QueryCondition condition) {
        this.andQueryConditions.add(condition);
        return this;
    }

    public QueryDetail<T> andAll(String queryConditionJson) {
        List<QueryCondition> list = null;
        if (PublicUtils.isNotEmpty(queryConditionJson)) {
            try {
                list = JSONArray.parseArray(queryConditionJson, QueryCondition.class);
            } catch (Exception e) {
                logger.warn(PublicUtils.toAppendStr("queryCondition[", queryConditionJson,
                        "] is not json or other error", e.getMessage()));
            }
        }
        if (list == null) {
            list = Lists.newArrayList();
        }
        List<QueryCondition> rsList = Lists.newArrayList(list);
        this.andQueryConditions.addAll(rsList);
        return this;
    }

    /**
     * 添加多个and条件
     *
     * @param condition 该条件
     * @return 链式调用
     */
    public QueryDetail<T> and(QueryCondition... condition) {
        this.andQueryConditions.addAll(Arrays.asList(condition));
        return this;
    }

    public QueryDetail<T> andAll(Collection<QueryCondition> conditions) {
        this.andQueryConditions.addAll(conditions);
        return this;
    }

    /**
     * 添加一个or条件
     *
     * @param condition 该条件
     * @return 链式调用
     */
    public QueryDetail<T> or(QueryCondition condition) {
        this.orQueryConditions.add(condition);
        return this;
    }

    /**
     * 添加多个or条件
     *
     * @param condition 该条件
     * @return 链式调用
     */
    public QueryDetail<T> or(QueryCondition... condition) {
        this.orQueryConditions.addAll(Arrays.asList(condition));
        return this;
    }

    public QueryDetail<T> orAll(Collection<QueryCondition> conditions) {
        if (PublicUtils.isNotEmpty(conditions)) {
            this.orQueryConditions.addAll(conditions);
        }
        return this;
    }

    public QueryDetail<T> orAll(String queryConditionJson) {
        List<QueryCondition> list = null;
        if (PublicUtils.isNotEmpty(queryConditionJson)) {
            try {
                list = JSONArray.parseArray(queryConditionJson, QueryCondition.class);
            } catch (Exception e) {
                logger.warn(PublicUtils.toAppendStr("queryCondition[", queryConditionJson,
                        "] is not json or other error", e.getMessage()));
            }
        }
        if (list == null) {
            list = Lists.newArrayList();
        }
        List<QueryCondition> rsList = Lists.newArrayList(list);
        this.orQueryConditions.addAll(rsList);
        return this;
    }

    /**
     * 升序字段
     *
     * @param property 该字段对应变量名
     * @return 链式调用
     */
    public QueryDetail<T> orderASC(String... property) {
        if (PublicUtils.isNotEmpty(property)) {
            for (int i = 0; i < property.length; i++) {
                this.orders.add(Order.asc(property[i]));
            }
        }
        return this;
    }

    /**
     * 降序字段
     *
     * @param property 该字段对应变量名
     * @return 链式调用
     */
    public QueryDetail<T> orderDESC(String... property) {
        if (PublicUtils.isNotEmpty(property)) {
            for (int i = 0; i < property.length; i++) {
                this.orders.add(Order.desc(property[i]));
            }
        }
        return this;
    }

    public QueryDetail<T> convertPageModel(PageModel pm) {
        this.andAll(pm.getQueryConditionJson());
        this.setOrders(pm.getOrders());
        pm.setOrders(null);
        pm.setSort(null);
        return this;
    }

    /**
     * 清除所有条件
     *
     * @return 该实例
     */
    public QueryDetail<T> clearAll() {
        if (!this.andQueryConditions.isEmpty()) {
            this.andQueryConditions.clear();
        }
        if (!this.orQueryConditions.isEmpty()) {
            this.orQueryConditions.clear();
        }
        if (!this.orders.isEmpty()) {
            this.orders.clear();
        }
        return this;
    }

    /**
     * 清除and条件
     *
     * @return 该实例
     */
    public QueryDetail<T> clearAnd() {
        if (!this.andQueryConditions.isEmpty()) {
            this.andQueryConditions.clear();
        }
        return this;
    }

    /**
     * 清除or条件
     *
     * @return 该实例
     */
    public QueryDetail<T> clearOr() {
        if (!this.orQueryConditions.isEmpty()) {
            this.andQueryConditions.clear();
        }
        return this;
    }

    /**
     * 清除order条件
     *
     * @return 该实例
     */
    public QueryDetail<T> clearOrder() {
        if (!this.orders.isEmpty()) {
            this.orders.clear();
        }
        return this;
    }

    public LambdaQueryWrapper<T> lambda() {
        //转换已有QueryCondition条件
        this.applyWrapper();
        return this.queryWrapper.lambda();
    }

    public QueryWrapper getRealQueryWrapper() {
        return this.queryWrapper;
    }

    public QueryWrapper getQueryWrapper() {
        if (this.andQueryConditions.size() > 0 || this.getOrders().size() > 0) {
            this.applyWrapper();
        }
        return this.queryWrapper;
    }

    public Wrapper<T> getWrapper() {
        return (Wrapper<T>) getQueryWrapper();
    }

    public void applyWrapper() {
        //转换已有QueryCondition条件
        QueryWrapperUtil.convertQueryDetail(this);
    }

    /**
     * 连接条件
     */
    public enum Condition {
        AND, OR
    }
}
