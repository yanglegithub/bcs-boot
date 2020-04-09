package com.phy.bcs.common.mvc.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.phy.bcs.common.mvc.domain.BaseModel;
import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;
import com.phy.bcs.common.mvc.repository.BaseRepository;
import com.phy.bcs.common.util.BeanConvertUtils;
import com.phy.bcs.common.util.PublicUtils;
import com.phy.bcs.common.util.domain.PageModel;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Service基类
 */
@Transactional(rollbackFor = Exception.class)
public abstract class DataService<repository extends BaseRepository<T>,
        T extends BaseModel, V extends BaseModelVo>
        extends BaseService<repository, T> {

    private Class<V> entityVoClz;

    public DataService() {
        super();
        Class<?> c = getClass();
        Type type = c.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] parameterizedType = ((ParameterizedType) type).getActualTypeArguments();
            entityVoClz = (Class<V>) parameterizedType[2];
        }
    }

    public Class<V> getEntityVoClz() {
        return entityVoClz;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public V findOneVo(String id) {
        return copyBeanToVo(getById(id));
    }

    public void copyBeanToVo(T module, V result) {
        if (result != null && module != null) {
            mapperFacade.map(module, result);
            //BeanVoUtil.copyProperties(module, result, true);
        }
    }

    public V copyBeanToVo(T module) {
        V result = null;
        if (module != null) {
            try {
                result = entityVoClz.newInstance();
                copyBeanToVo(module, result);
            } catch (Exception e) {
                log.error("{}", e);
            }
        }
        return result;
    }

    public List<V> copyBeanToVo(List<T> modules) {
        return BeanConvertUtils.beanToVoList(modules, entityVoClz);
    }

    public void copyVoToBean(V form, T entity) {
        if (form != null && entity != null) {
            mapperFacade.map(form, entity);
            //BeanVoUtil.copyProperties(form, entity, true);
        }
    }

    public T copyVoToBean(V form) {
        T result = null;
        if (form != null && getEntityClass() != null) {
            try {
                result = getEntityClass().newInstance();
                copyVoToBean(form, result);
            } catch (Exception e) {
                log.error("{}", e);
            }
        }
        return result;
    }

    public List<T> copyVoToBean(List<V> forms) {
        List<T> result = new ArrayList<>();
        if (PublicUtils.isNotEmpty(forms)) {
            for (V form : forms) {
                T resultOne = copyVoToBean(form);
                result.add(resultOne);
            }
        }
        return result;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<V> findAllVo() {
        List<V> resultVos = new ArrayList<>();
        List<T> result = list();
        if (PublicUtils.isNotEmpty(result)) {
            resultVos = BeanConvertUtils.beanToVoList(result, entityVoClz);
        }
        return resultVos;
    }

    public PageModel<V> findVoPage(PageModel pm) {
        PageModel pageModel = findPage(pm);
        List<T> list = (List) pageModel.getData();
        List<V> listVo = copyBeanToVo(list);
        pm.setData(listVo);
        return pm;
    }

    public boolean saveOrUpdate(V form) {
        return this.save(form);
    }

    public boolean save(V form) {
        T newEntity = null;
        try {
            newEntity = getEntityClass().newInstance();
            
            /*if (PublicUtils.isNotEmpty(form.getId())) {
                T originEntity = super.getById(form.getId());
                BeanVoUtils.copyProperties(originEntity, newEntity, true);
            }*/

            copyVoToBean(form, newEntity);
        } catch (Exception e) {
            log.warn("{}", e);
        }
        boolean success = saveOrUpdate(newEntity);

        if (success) {
            copyBeanToVo(newEntity, form);
        }
        return success;
    }


    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<V> findAllVo(Wrapper<T> wrapper) {
        List<V> resultVos = new ArrayList<>();
        List<T> result = super.findAll(wrapper);
        if (PublicUtils.isNotEmpty(result)) {
            resultVos = BeanConvertUtils.beanToVoList(result, entityVoClz);
        }
        return resultVos;
    }

    public boolean doCheckByProperty(V entityForm) {
        T entity = copyVoToBean(entityForm);
        return super.doCheckByProperty(entity);
    }

    public List<T> findAll() {
        return repository.selectList(null);
    }
}
