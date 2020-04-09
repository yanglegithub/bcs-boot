package com.phy.bcs.common.mvc.domain.query;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by somewhere on 2017/3/2.
 */
@Data
public class TreeResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String pid;
    private String label;
    private String value;
    private String key;
    private String iconCls;
    private String type;

}
