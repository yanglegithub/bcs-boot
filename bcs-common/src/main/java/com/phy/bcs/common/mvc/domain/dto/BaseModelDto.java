package com.phy.bcs.common.mvc.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通常的数据基类
 */
@Data
public class BaseModelDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /*** ID */
    public static final String F_ID = "id";
    /*private String id;*/
}
