package com.phy.bcs.service.ifs.controller;

import com.phy.bcs.common.rest.ResultBuilder;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/test")
public class TestController {

    @GetMapping(value = "/")
    @ApiOperation(value="测试")
    public @ResponseBody ResponseEntity test() {
        String result = "hello world";
        return ResultBuilder.buildOk(result);
    }

}
