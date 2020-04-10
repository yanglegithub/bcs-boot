package com.phy.bcs.service.ifs.controller.server;

import com.phy.bcs.service.ifs.controller.model.*;
import com.phy.bcs.service.ifs.controller.util.ParseUtil;

public class ParseModeToByte {
    /**
     * FEP协议解析
     */
    public static byte[] parseFEPTo(ParseFEP data){
        byte[] bytes = null;
        if(data.getFlag().equals("1")){
            bytes = "1".getBytes();
            bytes = ParseUtil.byteMerger(bytes,parseFEPSendTo(data.getSendFEPMode()));
        }else if(data.getFlag().equals("2")){
            bytes = "2".getBytes();
            bytes = ParseUtil.byteMerger(bytes,parseFEPAnswerTo(data.getAnswerFEPMode()));
        }else if(data.getFlag().equals("3")){
            bytes = "3".getBytes();
            bytes = ParseUtil.byteMerger(bytes,parseFEPFinishTo(data.getFinishFEPMode()));
        }else if(data.getFlag().equals("4")){
            bytes = "4".getBytes();
            bytes = ParseUtil.byteMerger(bytes,parseFEPDataTo(data.getDataFEPMode()));
        }
        return bytes;
    }
    public static byte[] parseFEPSendTo(SendFEPMode data){
        byte[] bytes = new byte[0];
        bytes = ParseUtil.byteMerger(bytes,data.getFileName().getBytes());
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.intToBytes2(data.getFileLength()));
        return bytes;
    }
    public static byte[] parseFEPAnswerTo(AnswerFEPMode data){
        byte[] bytes = new byte[0];
        bytes = ParseUtil.byteMerger(bytes,data.getFileName().getBytes());
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.intToBytes2(data.getNum()));
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.int2ToBytes(data.getID()));
        return bytes;

    }
    public static byte[] parseFEPFinishTo(FinishFEPMode data){
        byte[] bytes = new byte[0];
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.int2ToBytes(data.getID()));
        return bytes;
    }
    public static byte[] parseFEPDataTo(DataFEPMode data){
        byte[] bytes = new byte[0];
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.intToBytes2(data.getNum()));
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.int2ToBytes(data.getID()));
        bytes = ParseUtil.byteMerger(bytes,data.getData().getBytes());
        return bytes;
    }
    /**
     * RECP协议解析
     */
    public static byte[] parseRecpTo(ParseRECP data){
        byte[] bytes = null;
        if(data.getFlag().equals(PackageType.SYN)){
            bytes = new byte[]{0x01};
        }else if(data.getFlag().equals(PackageType.ACK)){
            bytes = new byte[]{0x02};
        }else if(data.getFlag().equals(PackageType.DATA)){
            bytes = new byte[]{0x04};
        }else if(data.getFlag().equals(PackageType.FIN)){
            bytes = new byte[]{0x08};
        }
        bytes = ParseUtil.byteMerger(bytes,getIpbyteFromStr( data.getSourceAddress()));
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.intToBytes2(data.getSerialNumber()));
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.strToBytes(data.getReservedBits(), 4));
        bytes = ParseUtil.byteMerger(bytes,ParseUtil.intToBytes2(data.getAbstractLength()));
        if(data.getAbstractLength() != 0 ){
            bytes = ParseUtil.byteMerger(bytes, ParseUtil.strToBytes(data.getAbstractData(), 56));
        }else{
            bytes = ParseUtil.byteMerger(bytes, new byte[56]);
        }
        bytes = data.getData()==null?bytes:ParseUtil.byteMerger(bytes, parseFEPTo(data.getData()));
        return bytes;
    }

    public static byte[] getIpbyteFromStr(String ipadress){
        String[] ips = ipadress.split("\\.");
        if(!ipadress.contains("."))
            ips = ipadress.split(":");
        byte[] ip = new byte[4];
        if(ips.length > 4){
            int h = Integer.parseInt(ips[4], 16);
            int l = Integer.parseInt(ips[5], 16);
            ip = new byte[]{(byte) (h/256), (byte) (h%256), (byte) (l/256), (byte) (l%256)};
        }else{
            for (int i = 0; i < 4; i++){
                ip[i] = (byte) Integer.parseInt(ips[i]);
            }
        }
        return ip;
    }

}
