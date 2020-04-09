package com.phy.bcs.service.ifs.controller.model;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import lombok.Data;

import java.io.UnsupportedEncodingException;

/**
 * 创建者：huangj
 * 类描述: FEP协议应答包
 */
@Data
public class AnswerFEPMode {
    //64 byte
    private String fileName;
    //偏移位置 4 byte
    private int num;
    //2byte
    private int ID;
    public AnswerFEPMode(byte[] bytes) throws UnsupportedEncodingException {
        byte[] fileNameBytes = ParseUtil.strChange(bytes, 0, 64);
        fileName = new String(fileNameBytes, "UTF-8");
        byte[] numBytes = ParseUtil.strChange(bytes, 64, 68);
        num = ParseUtil.bytesToInt2(numBytes, 0);
        byte[] idBytes = ParseUtil.strChange(bytes, 68, 69);
        ID = ParseUtil.bytes2ToInt(idBytes, 0);
    }
    public AnswerFEPMode(){

    }

}
