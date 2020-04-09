package com.phy.bcs.common.mvc.domain.vo;


import com.phy.bcs.common.util.annotation.DictType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictVo extends TreeEntityVo {

    public static final String F_CODE = "code";
    private static final long serialVersionUID = 1L;

    /*** 编码 */
    private String code;

    /*** 字典值 */
    private String val;

    /*** 资源文件key */
    private String showName;

    /*** key */
    private String key;

    @DictType(name = "sys_yes_no")
    private Integer isShow = 1;

    private String parentCode;

}
