package com.phy.bcs.service.ifs.controller.model;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import lombok.Data;

/**
 * 创建者：huangj
 * 类描述: FEP协议结束包
 */
@Data
public class FinishFEPMode {
    //2 byte
    private int ID;
    public FinishFEPMode(byte[] bytes) {
        byte[] idBytes = ParseUtil.strChange(bytes, 0, 2);
        ID = ParseUtil.bytes2ToInt(idBytes, 0);
    }
    public FinishFEPMode(){

    }
}
