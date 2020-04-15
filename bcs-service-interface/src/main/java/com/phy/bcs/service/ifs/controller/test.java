package com.phy.bcs.service.ifs.controller;

import com.phy.bcs.service.ifs.controller.model.DataFEPMode;
import com.phy.bcs.service.ifs.controller.model.ParseFEP;
import com.phy.bcs.service.ifs.controller.server.ParseModeToByte;
import java.io.UnsupportedEncodingException;

/**
 * 创建者：huangj
 * 类描述: 协议测试
 */
public class test {
    public static void main(String[] args) throws UnsupportedEncodingException {
       /* //RECP协议逆解析 无摘要
        ParseRECP parseRECPto = new ParseRECP();
        parseRECPto.setFlag(PackageType.DATA);
        parseRECPto.setSourceAddress("1174");
        parseRECPto.setSerialNumber(12);
        parseRECPto.setReservedBits("1234");
        parseRECPto.setAbstractLength(0);
        parseRECPto.setData("RECP协议解析");
        System.out.println(parseRECPto.toString());
        byte[] bytes = ParseModeToByte.parseRecpTo(parseRECPto);
        System.out.println(bytes);
        //RECP协议解析
        ParseRECP parseRECP = new ParseRECP(bytes);
        System.out.println(parseRECP);*/
       //FEP协议逆解析 数据包测试
        ParseFEP parseFEPto = new ParseFEP();
        parseFEPto.setFlag(4);
        DataFEPMode dataFEPMode = new DataFEPMode();
        dataFEPMode.setNum(1234);
        dataFEPMode.setID(67);
        dataFEPMode.setData("FEP协议数据包".getBytes());
        parseFEPto.setDataFEPMode(dataFEPMode);
        byte[] bytes = ParseModeToByte.parseFEPTo(parseFEPto);
        System.out.println(bytes);
        //FEP协议解析
        ParseFEP parseFEP = new ParseFEP(bytes);
        System.out.println(parseFEP);


    }
}
