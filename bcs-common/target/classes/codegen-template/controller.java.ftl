/**
* Copyright &copy; 2019-2020  All rights reserved.
*/
package ${package.Controller};

import javax.validation.Valid;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.phy.mpss.common.constant.GlobalConstants;
import com.phy.mpss.common.rest.ResultBuilder;
import com.phy.mpss.common.util.JsonUtils;
import com.phy.mpss.common.util.StringUtils;
import com.phy.mpss.common.util.domain.PageModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ${package.Entity}.vo.${entity}Vo;
import ${package.Service}.${table.serviceName};
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if swagger2>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
 * <p>
 * ${table.comment!} 控制器
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if swagger2>
@Api(tags = "<#if package.ModuleName??>${package.ModuleName}</#if>_${entity}")
</#if>
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass}<${table.serviceName}, ${entity}Vo> {
<#else>
public class ${table.controllerName} {
</#if>
    public ${table.controllerName}(${table.serviceName} service) {
        super(service);
    }

    @GetMapping(value = "/")
    @ApiOperation(value = "${table.comment!}分页查询", notes = "根据分页参数获取分页列表")
    public ResponseEntity getPage(PageModel pm) {
        service.findVoPage(pm);
        JSON json = JsonUtils.getInstance().toJsonObject(pm);
        return ResultBuilder.buildObject(json);
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${table.comment!}保存", notes = "${table.comment!}保存")
    public ResponseEntity saveOrUpdate(@Valid @RequestBody ${entity}Vo vo) {
        service.saveOrUpdate(vo);
        return ResultBuilder.buildOk("保存${table.comment!}成功");
    }

    @DeleteMapping(value = "/{ids:" + GlobalConstants.ENTITY_ID_REGEX
    + "}")
    @ApiOperation(value = "${table.comment!}删除", notes = "${table.comment!}删除")
    public ResponseEntity delete(@PathVariable String ids) {
        service.removeByIds(Lists.newArrayList(ids.split(StringUtils.SPLIT_DEFAULT)));
        return ResultBuilder.buildOk("删除成功");
    }
}
</#if>
