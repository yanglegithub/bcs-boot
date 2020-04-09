/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.phy.bcs.common.mvc.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 实体父类
 */
@Data
public abstract class BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /*** ID */
    public static final String F_ID = "id";
    public static final String F_STATUS = "status";
    public static final String F_SQL_ID = "ID_";
    public static final String F_SQL_STATUS = "STATUS_";
    /*** 状态 正常 */
    public static final Integer FLAG_NORMAL = 0;
    /*** 状态 已删除 */
    public static final Integer FLAG_DELETE = -1;

    //@TableId(value = F_SQL_ID, type = IdType.UUID)
    //protected String id;
}
