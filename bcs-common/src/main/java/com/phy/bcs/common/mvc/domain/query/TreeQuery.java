package com.phy.bcs.common.mvc.domain.query;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * Created by somewhere on 2017/3/2.
 */
@Data
@ApiModel
@ToString
public class TreeQuery {

    private String extId;
    private String all;

}
