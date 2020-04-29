package com.phy.bcs.service.records.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.phy.bcs.common.mvc.domain.BaseModel;
import com.phy.bcs.service.records.model.typehandler.LocalDateTimeTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.ByteArrayTypeHandler;

/**
 * <p>
 *
 * </p>
 *
 * @author yangl
 * @since 2020-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "record",autoResultMap = true)
public class Record extends BaseModel {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("type")
    private String type;

    @TableField( value = "create_time", typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime createTime;

    @TableField("dest_sys")
    private Integer destSys;

    @TableField( "file_name")
    private String fileName;

    @TableField( "path")
    private String path;

    @TableField("dest_prot")
    private String destProt;

    @TableField( value = "prot_head", typeHandler = ByteArrayTypeHandler.class)
    private byte[] protHead;

    @TableField( value = "prot_body", typeHandler = ByteArrayTypeHandler.class)
    private byte[] protBody;

    @TableField("is_success")
    private Integer isSuccess;

    @TableField(value = "success_time", typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime successTime;


}
