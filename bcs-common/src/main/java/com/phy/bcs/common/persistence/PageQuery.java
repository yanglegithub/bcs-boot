package com.phy.bcs.common.persistence;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.phy.bcs.common.persistence.util.SqlUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;

/**
 * 页查询工具
 */
public class PageQuery<T> extends Page<T> {

    private static final String PAGE = "page";
    private static final String LIMIT = "limit";
    private static final String ORDER_BY_FIELD = "orderByField";
    private static final String IS_ASC = "isAsc";

    public PageQuery(Class<T> clazz, Pageable pageable) {

        super(pageable.getPageNumber()
                , pageable.getPageSize());

        if (pageable.getSort() != null) {
            Iterator<Sort.Order> iterator = pageable.getSort().iterator();
            List ascList = Lists.newArrayList();
            List descList = Lists.newArrayList();
            while (iterator.hasNext()) {
                Sort.Order order = iterator.next();
                String sqlField = SqlUtils.getSqlColumnByProperty(clazz, order.getProperty());

                if (order.getDirection().isAscending()) {
                    ascList.add(sqlField);
                } else if (order.getDirection().isDescending()) {
                    descList.add(sqlField);
                }
            }
            this.setAscs(ascList);
            this.setDescs(descList);
        }
    }
}
