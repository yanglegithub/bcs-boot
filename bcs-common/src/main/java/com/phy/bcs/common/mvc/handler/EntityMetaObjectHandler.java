package com.phy.bcs.common.mvc.handler;/*
package com.phy.bcs.common.mvc.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.phy.bcs.common.mvc.domain.BaseEntity;
import com.phy.bcs.common.com.phy.bcs.api.util.PublicUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.com.phy.bcs.api.util.Date;

*/
/**
 * 自动填充 (主要用更新公共字段）
 *
 * @author lijie
 *//*

@Component
public class EntityMetaObjectHandler implements MetaObjectHandler {

    */
/**
     * 自动填充插入信息
     *//*

    @Override
    public void insertFill(MetaObject metaObject) {
//        String auditorId = (String)auditorAware.getCurrentAuditor().orElse(SecurityConstants.ADMIN_ID);
        String auditorId = "1";
        setInsertFieldValByName(BaseEntity.F_CREATEDBY, auditorId, metaObject);
        Date date = PublicUtils.getCurrentDate();
        setInsertFieldValByName(BaseEntity.F_CREATEDDATE, date, metaObject);
        setInsertFieldValByName(BaseEntity.F_LASTMODIFIEDBY, auditorId, metaObject);
        setInsertFieldValByName(BaseEntity.F_LASTMODIFIEDDATE, date, metaObject);

    }

    */
/**
     * 自动填充更新信息
     *//*

    @Override
    public void updateFill(MetaObject metaObject) {
//        String auditorId = (String)auditorAware.getCurrentAuditor().orElse(SecurityConstants.ADMIN_ID);
        String auditorId = "1";
        Date date = PublicUtils.getCurrentDate();
        setInsertFieldValByName(BaseEntity.F_LASTMODIFIEDBY, auditorId, metaObject);
        setInsertFieldValByName(BaseEntity.F_LASTMODIFIEDDATE, date, metaObject);
    }
}
*/
