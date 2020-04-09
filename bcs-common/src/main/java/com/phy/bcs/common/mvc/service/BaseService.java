package com.phy.bcs.common.mvc.service;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.phy.bcs.common.mvc.domain.BaseModel;
import com.phy.bcs.common.mvc.repository.BaseRepository;
import com.phy.bcs.common.persistence.PageQuery;
import com.phy.bcs.common.persistence.QueryDetail;
import com.phy.bcs.common.persistence.util.Querys;
import com.phy.bcs.common.util.PublicUtils;
import com.phy.bcs.common.util.QueryUtils;
import com.phy.bcs.common.util.base.Reflections;
import com.phy.bcs.common.util.domain.PageModel;
import com.phy.bcs.common.util.domain.QueryCondition;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Service基类
 */
@Transactional(rollbackFor = Exception.class)
public abstract class BaseService<repository extends BaseRepository<T>,
    T extends BaseModel> extends ServiceImpl<repository, T> {

    public final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    public repository repository;

    @Autowired
    protected MapperFacade mapperFacade;

    private Class<T> entityClass;

    public BaseService() {
        Class<?> c = getClass();
        Type type = c.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] parameterizedType = ((ParameterizedType) type).getActualTypeArguments();
            entityClass = (Class<T>) parameterizedType[1];
        }
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * 获取实体的表名
     *
     * @return
     */
    public String findEntityTableName() {
        TableName table = getEntityClass().getAnnotation(TableName.class);
        String tableName = table.value().toString();
        return tableName;
    }

    public PageModel<T> findPage(PageModel<T> pm) {
        return findPageQuery(pm, null);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageModel<T> findPageQuery(PageModel<T> pm, List<QueryCondition> queryConditions) {
        //构造外来条件
        QueryDetail<T> queryDetail = createQuery();
        queryDetail.convertPageModel(pm);

        return findPageWrapper(pm, queryDetail.getWrapper());
    }

    public QueryDetail<T> createQuery(List<QueryCondition> queryConditionList) {
        return Querys.createQuery(getEntityClass(), queryConditionList);
    }

    /**
     * 调用自定义条件构造器
     */
    public QueryDetail<T> createQuery() {
        return Querys.createQuery(getEntityClass());
    }

    public QueryDetail<T> createQuery(String mainTableAlias) {
        return Querys.createQuery(getEntityClass(), mainTableAlias);
    }


    /**
     * 动态集合查询
     *
     * @param queryDetail 动态条件构造对象
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<T> findAll(QueryDetail<T> queryDetail) {
        return findAll(queryDetail.getWrapper());
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<T> findAll(Wrapper<T> wrapper) {
        return super.list(wrapper);
    }


    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageModel<T> findPage(PageModel<T> pm, List<QueryCondition> queryConditions) {
        return findPageQuery(pm, queryConditions);
    }


    /**
     * 动态分页查询
     *
     * @param pm      分页对象
     * @param wrapper 动态条件对象
     * @return
     */
    private PageModel<T> findPageWrapper(PageModel<T> pm, Wrapper<T> wrapper) {
        try {
            IPage page = page(new PageQuery(getEntityClass(), pm), wrapper);
            pm.setData(page.getRecords());
            pm.setRecordsTotal(page.getTotal());
            return pm;
        } catch (Exception e) {
            log.error("error: {}", e);
            throw e;
        }
    }


    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public T findOne(String id) {
        return super.getById(id);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageModel<T> findPage(PageModel<T> pm, Wrapper<T> wrapper, QueryCondition... queryConditions) {
        //构造条件
        QueryDetail<T> queryDetail = new QueryDetail(getEntityClass(), null, wrapper);
        queryDetail.setAndQueryConditions(pm.findQueryConditions());
        queryDetail.and(queryConditions);
        //queryDetail.lambda().ne(BaseModel::getStatus, GeneralEntity.FLAG_DELETE);

        return findPageWrapper(pm, queryDetail.getWrapper());
    }


    /**
     * 动态分页查询
     *
     * @param pm          分页对象
     * @param queryDetail 动态条件对象
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageModel<T> findPage(PageModel<T> pm, QueryDetail<T> queryDetail) {
        if (queryDetail != null) {
            queryDetail.andAll(pm.findQueryConditions());
            return findPageWrapper(pm, queryDetail.getWrapper());

        }
        return findPageWrapper(pm, null);
    }

    public boolean doCheckByProperty(T entity) {
        Map<String, QueryCondition.Operator> maps = Maps.newHashMap();
        try {
            maps.put(BaseModel.F_ID, QueryCondition.Operator.ne);
            maps.put(BaseModel.F_STATUS, QueryCondition.Operator.ne);
            Reflections.setProperty(entity, BaseModel.F_STATUS, BaseModel.FLAG_DELETE);
        } catch (Exception e) {
            log.error("{}", e);
        }
        return doCheckWithEntity(entity, maps);
    }

    public boolean doCheckWithEntity(T entity, Map<String, QueryCondition.Operator> maps) {
        boolean rs = false;
        if (PublicUtils.isNotEmpty(entity)) {
            List<QueryCondition> conditionList = QueryUtils
                .convertObjectToQueryCondition(entity, maps);
            Wrapper queryWrapper = Querys.createQuery(entity.getClass(), conditionList).getWrapper();
            Integer obj = count(queryWrapper);
            if (obj == null || obj == 0) {
                rs = true;
            }
        }
        return rs;
    }


}
