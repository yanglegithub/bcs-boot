package com.phy.bcs.service.ifs.controller.model;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import lombok.Data;

import java.io.UnsupportedEncodingException;

/**
 * 创建者：huangj
 * 类描述: FEP协议数据包
 */
@Data
public class DataFEPMode {
    //偏移位置 4 byte
    private int num;
    //2byte
    private int ID;
    //n byte
    private byte[] data;
    public DataFEPMode(byte[] bytes) throws UnsupportedEncodingException {
        byte[] numBytes = ParseUtil.strChange(bytes, 0, 4);
        num = ParseUtil.bytesToInt2(numBytes, 0);
        byte[] idBytes = ParseUtil.strChange(bytes, 4, 6);
        ID = ParseUtil.bytes2ToInt(idBytes, 0);
        byte[] sourceAddressByte = ParseUtil.strChange(bytes, 6, bytes.length);
        data = sourceAddressByte;
    }
    public DataFEPMode(){

    }
}
