/**
* Copyright &copy; 2019-2020  All rights reserved.
*/
package com.phy.bcs.service.records.controller;

import javax.sql.rowset.serial.SerialBlob;
import javax.validation.Valid;

import ch.qos.logback.core.pattern.color.BoldBlueCompositeConverter;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.phy.bcs.common.constant.GlobalConstants;
import com.phy.bcs.common.mvc.controller.DataResource;
import com.phy.bcs.common.rest.ResultBuilder;
import com.phy.bcs.common.util.JsonUtils;
import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.common.util.domain.PageModel;
import com.phy.bcs.service.ifs.config.Constants;
import com.phy.bcs.service.records.model.Record;
import com.phy.bcs.service.records.model.vo.RecordVo;
import com.phy.bcs.service.records.service.RecordService;
import org.apache.ibatis.type.JdbcType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 *  控制器
 * </p>
 *
 * @author yangl
 * @since 2020-04-28
 */
@RestController
@RequestMapping("/log/record")
@Api(tags = "log_Record")
public class RecordController extends DataResource<RecordService, RecordVo> {
    public RecordController(RecordService service) {
        super(service);
    }

    @GetMapping(value = "/")
    @ApiOperation(value = "分页查询", notes = "根据分页参数获取分页列表")
    public ResponseEntity getPage(PageModel pm) {
        service.findVoPage(pm);
        JSON json = JsonUtils.getInstance().toJsonObject(pm);
        return ResultBuilder.buildObject(json);
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存", notes = "保存")
    public ResponseEntity saveOrUpdate(@Valid @RequestBody RecordVo vo) {
        service.saveOrUpdate(vo);
        return ResultBuilder.buildOk("保存成功");
    }

    @DeleteMapping(value = "/{ids:" + GlobalConstants.ENTITY_ID_REGEX
    + "}")
    @ApiOperation(value = "删除", notes = "删除")
    public ResponseEntity delete(@PathVariable String ids) {
        service.removeByIds(Lists.newArrayList(ids.split(StringUtils.SPLIT_DEFAULT)));
        return ResultBuilder.buildOk("删除成功");
    }

    @GetMapping(value = "/insertOne")
    @ApiOperation(value = "插入测试", notes = "这是个插入测试，测试框架是否成功运行")
    public ResponseEntity insertOne() throws SQLException {
        Record record = new Record();
        record.setCreateTime(LocalDateTime.now());
        record.setType("rec");
        record.setDestSys(Constants.TFC_SYSTEM);
        record.setFileName("hello.txt");
        record.setPath("./.ftp-received-bak");
        record.setDestProt("FEP");
        record.setIsSuccess(1);
        record.setSuccessTime(LocalDateTime.now());
        record.setProtHead((new byte[]{0x32, 0x33,0x33,0x34,0x35,0x36,0x37,0x38}));
        record.setProtBody((new byte[]{0x42,0x41, 0x43}));
        service.save(record);
        return ResultBuilder.buildOk("保存成功");
    }
}
