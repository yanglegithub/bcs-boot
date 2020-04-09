package com.phy.bcs.common.mvc.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 通常的数据基类
 */
@Data
public class BaseModelVo implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String F_ID = "id";

    /*** ID */
    //private String id;
}
