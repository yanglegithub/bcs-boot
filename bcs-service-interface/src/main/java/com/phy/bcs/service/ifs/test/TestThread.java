package com.phy.bcs.service.ifs.test;


import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class TestThread {
    public static int s_count = 10000;
    public int count = 10;

    public synchronized void test1(){
        for(int i=0; i<500; i++){
            s_count--;
            System.out.println(Thread.currentThread().getName() + "-" + s_count);
        }
    }

    public synchronized void test(){
        s_count--;
        System.out.println(Thread.currentThread().getName() + "-" + s_count);
    }

    public synchronized void test2(){
        count--;
        System.out.println(Thread.currentThread().getName() + "-" + count);
    }

    public void test3(){
        count--;
        System.out.println(Thread.currentThread().getName() + "-" + count);
    }

    public synchronized static void test4(){
        s_count--;
        System.out.println(Thread.currentThread().getName() + "-" + s_count);
    }

    public static void test5(){
        s_count--;
        System.out.println(Thread.currentThread().getName() + "-" + s_count);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        /*byte a = (byte) 0x80;
        byte b = 0x0F;
        System.out.println("b="+b+",a="+a);
        System.out.printf("%010x,%010x\n", b, a);
        System.out.printf("%010x,%010x\n", (int)b, (int)a);

        byte[] aa = {0x10, 0x08, 0x00, 0x00};
        byte[] bb = {0x01, 0x01, 0x00, 0x00};
        int iaa = new BigInteger(aa).intValue();
        int ibb = new BigInteger(bb).intValue();

        System.out.println("aa="+iaa);
        System.out.println("bb="+ibb);
        System.out.printf("%010x,%010x\n", iaa, ibb);*/
        /*int a = 0x00111111;
        int b = 0x00001111;
        int c = 0x00000111;
        int d = 0xffffff80;
        System.out.println(new BigInteger(new Integer(a)).toByteArray());*/
        /*String str = "1234567890abcdefghijklmnopqrstuvwxyz";
        byte[] strbyte = str.getBytes();
        System.out.println(strbyte);

        byte[] mybytes = new byte[]{0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        String mystr = new String(mybytes, "UTF8");
        System.out.println("is equal:"+"2111".contains(mystr));
        byte[] mybytes_  = mystr.getBytes();
        System.out.println(mystr);*/
        byte[] bytes = new byte[0];
        System.out.println(bytes);
        System.out.println(bytes.length);
        int step = 0;
        if(step == 0){
            System.out.println(step);
            step = 1;
        }else if(step == 1){
            System.out.println(step);
            step = 2;
        }
    }
}
