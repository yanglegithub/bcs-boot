package com.phy.bcs.common.mvc.domain.vo;


import com.phy.bcs.common.util.annotation.BeanField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通常的数据基类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TreeEntityVo extends BaseModelVo {
    public static final String F_NAME = "name";
    public static final String F_PARENTID = "parentId";
    public static final String F_PARENTIDS = "parentIds";
    public static final String F_ISLEAF = "isLeaf";
    public static final String F_SORT = "sort";
    public static final String F_PARENT = "parent";
    /*** 模块名称 */
    protected String name;
    /*** 上级模块 */
    protected String parentId;
    /*** 上级模块 */
    @BeanField
    protected String parentIds;
    /*** 序号 */
    protected Integer sort;
    /*** 父模块名称 */
    @BeanField
    private String parentName;
    @BeanField
    private boolean isLeaf;

}
