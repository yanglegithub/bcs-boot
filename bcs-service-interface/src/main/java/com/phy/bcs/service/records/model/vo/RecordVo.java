package com.phy.bcs.service.records.model.vo;

import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;

import java.sql.Blob;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="RECORD对象", description="")
public class RecordVo extends BaseModelVo {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String type;

    private LocalDateTime createTime;

    private Integer destSys;

    private String fileName;

    private String path;

    private String destProt;

    private byte[] protHead;

    private byte[] protBody;

    private Integer isSuccess;

    private LocalDateTime successTime;


}
