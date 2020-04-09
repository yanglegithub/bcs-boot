package com.phy.bcs.common.util.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ComboData implements Serializable {

    public static final String F_NAME = "name";
    public static final String F_ID = "id";
    public static final String F_PID = "pId";
    public static final String F_CODE = "code";
    public static final String F_KEY = "key";
    private static final long serialVersionUID = 1L;
    private String id; // 下拉列表隐藏值
    private String name; // 下拉列表显示值
    private String pId; // 树形结构父节点
    private String code;
    private String key;

    public ComboData() {
    }

    public ComboData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ComboData(String id, String name, String pId) {
        this.id = id;
        this.name = name;
        this.pId = pId;
    }

    public ComboData(String id, String name, String code, String key) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.key = key;
    }


}
