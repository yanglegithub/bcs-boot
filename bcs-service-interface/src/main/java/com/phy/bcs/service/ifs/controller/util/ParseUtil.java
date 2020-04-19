package com.phy.bcs.service.ifs.controller.util;

import java.io.*;

/**
 * 创建者：huangj
 * 类描述: 协议解析和封装工具类
 */
public class ParseUtil {
    /**
     * 获得指定文件的byte数组
     * @param filePath 文件绝对路径
     * @return
     */
    public static byte[] file2Byte(String filePath){
        ByteArrayOutputStream bos=null;
        BufferedInputStream in=null;
        try{
            File file=new File(filePath);
            if(!file.exists()){
                throw new FileNotFoundException("file not exists");
            }
            bos=new ByteArrayOutputStream((int)file.length());
            in=new BufferedInputStream(new FileInputStream(file));
            int buf_size=1024;
            byte[] buffer=new byte[buf_size];
            int len=0;
            while(-1 != (len=in.read(buffer,0,buf_size))){
                bos.write(buffer,0,len);
            }
            return bos.toByteArray();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        finally{
            try{
                if(in!=null){
                    in.close();
                }
                if(bos!=null){
                    bos.close();
                }
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
    /**
     * 将int数值转换为占二个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] int2ToBytes(int value)
    {
        byte[] src = new byte[2];
        src[0] = (byte) ((value>>8)&0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }
    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }
    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。int2ToBytes（）配套使用
     */
    public static int bytes2ToInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF)<<8)
                |(src[offset+1] & 0xFF));
        return value;
    }
    //取byte数组部分值
    public static byte[] strChange(byte str1[],  int start, int end){

        int k = end - start;
        byte str2[] = new byte[k];
        for(int i = start, j=0 ; i<end && j<k; i++,j++){
            str2[j] = str1[i];
        }

        return str2;
    }
    //System.arraycopy()方法
    public static byte[] byteMerger(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }


    public static int setBytes(byte[] src, int off, byte[] target){
        if(target.length + off >= src.length){
            src = byteMerger(src, new byte[off+target.length - src.length]);
        }
        for (int i = off; i < target.length; i++){
            src[i] = target[i - off];
        }
        return off+target.length;
    }

    public static byte[] getBytes(byte[] src, int off, int length){
        byte[] r = new byte[length];
        for(int i = off; i < length + off; i++){
            r[i-off] = src[i];
        }
        return r;
    }
    //字符串转换成指定长度的字节数组，不够长度添加0x00,超出长度舍弃
    public static byte[] strToBytes(String str, int length){
        byte[] strbytes = str.getBytes();
        byte[] result = new byte[length];
        if(strbytes.length > length){
            System.arraycopy(strbytes, 0, result, 0, length);
        }else if(strbytes.length < length){
            System.arraycopy(strbytes, 0, result, 0, strbytes.length);
        }else {
            result = strbytes;
        }
        return result;
    }
}
