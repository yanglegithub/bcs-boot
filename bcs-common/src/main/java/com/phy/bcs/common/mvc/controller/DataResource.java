package com.phy.bcs.common.mvc.controller;

import com.phy.bcs.common.constant.GlobalConstants;
import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;
import com.phy.bcs.common.mvc.service.DataService;
import com.phy.bcs.common.rest.CustomMessage;
import com.phy.bcs.common.rest.ResultBuilder;
import com.phy.bcs.common.util.PublicUtils;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 基础控制器支持类
 * @author lijie
 */
@Slf4j
public class DataResource<Service extends DataService, V extends BaseModelVo>
        extends GeneralResource {

    protected final Service service;

    public DataResource(Service service) {
        this.service = service;
    }


    @ModelAttribute
    public V getAttribute(@RequestParam(required = false) String id, HttpServletRequest request) throws Exception {
        String path = request.getRequestURI();
        if (path != null && !path.contains(GlobalConstants.URL_CHECKBY) && !path.contains(GlobalConstants.URL_FIND) &&
                PublicUtils.isNotEmpty(id)) {
            return (V) service.findOneVo(id);
        } else {
            return (V) service.getEntityVoClz().newInstance();
        }
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/{id:" + GlobalConstants.ENTITY_ID_REGEX + "}")
    @ApiImplicitParam(name = "id", value = "实体ID", required = true, dataType = "String", paramType = "path")
    public ResponseEntity<CustomMessage<V>> get(@PathVariable String id) {
        log.debug("REST request to get Entity : {}", id);
        return ResultBuilder.wrapOrNotFound(Optional.ofNullable((V) service.findOneVo(id)));
    }

    @ResponseBody
    @GetMapping(value = "checkByProperty")
    public boolean checkByProperty(@ModelAttribute V entityForm) {
        return service.doCheckByProperty(entityForm);
    }

}
