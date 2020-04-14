package com.phy.bcs.service.ifs.test;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;

import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

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
        getUserIn("maintest");
    }

    public static boolean getUserIn(String tip){
        byte[] a = new byte[11];
        byte[] b = new byte[]{0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11};
        ParseUtil.setBytes(a, 2, b);
        System.out.println(a);
        return false;
    }
}
