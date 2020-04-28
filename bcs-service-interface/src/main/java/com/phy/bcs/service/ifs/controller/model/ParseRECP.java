package com.phy.bcs.service.ifs.controller.model;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import lombok.Data;

import java.io.UnsupportedEncodingException;

/**
 * 创建者：huangj
 * 类描述: RECP协议解析
 */
@Data
public class ParseRECP {
    //标志
    private PackageType flag;
    //源IP地址
    private String sourceAddress;
    //序号
    private int serialNumber;
    //保留位
    private String reservedBits;
    //摘要长度
    private int abstractLength;
    //摘要
    private String abstractData;
    //数据
    private ParseFEP data;

    public ParseRECP(byte[] bytes) throws UnsupportedEncodingException {
        String fl = new String("UTF-8");
        int flagInt = ParseUtil.strChange(bytes, 0, 1)[0];
        switch (flagInt) {
            case 1:
                flag = PackageType.SYN;
                break;
            case 2:
                flag = PackageType.ACK;
                break;
            case 4:
                flag = PackageType.DATA;
                break;
            case 8:
                flag = PackageType.FIN;
                break;
            default:
                break;
        }
        //第2 3 4 5 字节
        byte[] sourceAddressByte = ParseUtil.strChange(bytes, 1, 5);
        sourceAddress = getIpFrom4Byte(sourceAddressByte);
        //第6 7 8 9字节
        byte[] serialNumberBytes = ParseUtil.strChange(bytes, 5, 9);
        serialNumber = ParseUtil.bytesToInt2(serialNumberBytes, 0);
        //第10 11 12 13字节
        byte[] reservedBitsByte = ParseUtil.strChange(bytes, 9, 13);
        reservedBits = new String(reservedBitsByte, "UTF-8");
        //第14,15,16,17字节
        byte[] abstractLengthByte = ParseUtil.strChange(bytes, 13, 17);
        abstractLength = ParseUtil.bytesToInt2(abstractLengthByte, 0);
        if(abstractLength != 0){
            //第18-73字节 摘要
            byte[] srtbyte = ParseUtil.strChange(bytes, 17, 17 + abstractLength);
            abstractData = new String(srtbyte, "UTF-8");
        }else{
            abstractData = null;
        }
        //只有数据包有数据区
        if(flagInt == 4){
            //剩余n位字节 数据
            byte[] dataByte = ParseUtil.strChange(bytes, 17 + abstractLength, bytes.length);;
            data = new ParseFEP(dataByte);//byte[] 转 string
        }else{
            data = null;
        }


    }
    public ParseRECP(){}

    public String getIpFrom4Byte(byte[] ipbytes){
        String ip = "";
        for (int i = 0; i < 4; i++){
            int ipcount = 0x000000FF & (byte)ipbytes[i];
            ip += ip.equals("")?ipcount:("."+ipcount);
        }
        return ip;
    }
}
