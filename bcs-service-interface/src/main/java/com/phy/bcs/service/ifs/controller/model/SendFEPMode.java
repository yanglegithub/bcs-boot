package com.phy.bcs.service.ifs.controller.model;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import lombok.Data;

import java.io.UnsupportedEncodingException;

/**
 * 创建者：huangj
 * 类描述: FEP协议发送包
 */
@Data
public class SendFEPMode {
    //64 byte
    private String fileName;
    //4 byte
    private int fileLength;
    public SendFEPMode(byte[] bytes) throws UnsupportedEncodingException {
        byte[] fileNameBytes = ParseUtil.strChange(bytes, 0, 64);
        fileName = new String(fileNameBytes, "UTF-8");
        byte[] fileLengthBytes = ParseUtil.strChange(bytes, 64, 68);
        fileLength = ParseUtil.bytesToInt2(fileLengthBytes, 0);
    }
    public SendFEPMode(){

    }
}
