package com.phy.bcs.service.ifs.ftp.camel.util;

import java.io.File;

public class FileUtils {

    /**
     * 创建文件夹，支持多级
     *
     * @param file
     */
    public static void mkdirs(File file) {
        file.mkdirs();
    }

    /**
     * 创建文件夹，支持多级
     *
     * @param path
     */
    public static void mkdirs(String path) {
        mkdirs(new File(path));
    }

    public static void initLocalDir(String localDir) {
        if (!new File(localDir).exists()) {
            FileUtils.mkdirs(localDir);
        }
    }

    public static void main(String[] args) {
        mkdirs("./.ftp-send/test/aaaaaa/aaaaa/");
        mkdirs("../.ftp-sent-bak/test");
    }


}
