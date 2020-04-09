package com.phy.bcs.service.file.model.vo;

import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;
import com.phy.bcs.common.mvc.domain.BaseModel;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.sql.Blob;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author yangl
 * @since 2020-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value="对象", description="")
public class InfFileStatusVo extends BaseModelVo {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件ID")
    private BigDecimal id;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "文件长")
    private BigDecimal length;

    @ApiModelProperty(value = "文件内容")
    private Blob fileContent;

    @ApiModelProperty(value = "源系统 代码")
    private BigDecimal fromSystem;

    @ApiModelProperty(value = "目标系统 代码")
    private BigDecimal toSystem;

    @ApiModelProperty(value = "是否接收完成0:否 1:是")
    private BigDecimal recFinish;

    @ApiModelProperty(value = "是否发送完成0:否 1:是")
    private BigDecimal sendFinish;

    @ApiModelProperty(value = "重传次数（发送）")
    private BigDecimal transTimes;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;


}
