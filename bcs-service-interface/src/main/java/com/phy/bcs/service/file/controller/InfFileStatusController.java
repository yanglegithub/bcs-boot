/**
* Copyright &copy; 2019-2020  All rights reserved.
*/
package com.phy.bcs.service.file.controller;

import javax.sql.rowset.serial.SerialBlob;
import javax.validation.Constraint;
import javax.validation.Valid;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.phy.bcs.common.constant.GlobalConstants;
import com.phy.bcs.common.rest.ResultBuilder;
import com.phy.bcs.common.util.JsonUtils;
import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.common.util.domain.PageModel;
import com.phy.bcs.service.file.model.InfFileStatus;
import com.phy.bcs.service.file.model.vo.InfFileStatusVo;
import com.phy.bcs.service.file.service.InfFileStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.phy.bcs.common.mvc.controller.DataResource;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  控制器
 * </p>
 *
 * @author yangl
 * @since 2020-04-02
 */
@RestController
@RequestMapping("/file/inf-file-status")
@Api(tags = "file_InfFileStatus")
public class InfFileStatusController extends DataResource<InfFileStatusService, InfFileStatusVo> {

    @Autowired
    private InfFileStatusService infFileStatusService;

    public InfFileStatusController(InfFileStatusService service) {
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
    public ResponseEntity saveOrUpdate(@Valid @RequestBody InfFileStatusVo vo) {
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

    @GetMapping(value = "/all")
    @ApiOperation(value = "全部查询", notes = "获取全部列表")
    public ResponseEntity findAll(){
        List<InfFileStatus> list =  service.findAll();
        JSON json = JsonUtils.getInstance().toJsonObject(list);
        return ResultBuilder.buildObject(json);
    }

    @GetMapping(value = "/insertOne")
    @ApiOperation(value = "测试", notes = "测试方法")
    public ResponseEntity insertOne() throws SQLException {
        InfFileStatus file = new InfFileStatus();
        file.setFileName("test.txt");
        file.setLength(256);
        file.setFileContent(new byte[]{1,2,3,4});
        file.setFromSystem(0);
        file.setToSystem(1);
        file.setFromProto("FEP");
        file.setToProto("FEP");
        file.setRecFinish(0);
        file.setSendFinish(0);
        file.setTransTimes(0);
        file.setCreateTime(new Date());
        file.setUpdateTime(new Date());
        infFileStatusService.save(file);
        System.out.println(file.getId());
        return ResultBuilder.buildOk("保存成功");
    }

   @GetMapping(value = "/findAllM")
   public ResponseEntity findAllM(){
        List<InfFileStatus> list = infFileStatusService.findAllObject();
        return ResultBuilder.buildOk(list, "success");
    }

}
