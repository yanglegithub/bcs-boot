package com.phy.bcs.service.file.model;

import com.phy.bcs.common.mvc.domain.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableField;
import com.phy.bcs.service.ifs.ftp.camel.util.FileUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author yangl
 * @since 2020-04-02
 */
@Data
@Slf4j
//@EqualsAndHashCode(callSuper = true)
//@Accessors(chain = true)
//@TableName("INF_FILE_STATUS")
public class InfFileStatus extends BaseModel {

    private static final long serialVersionUID = 1L;

    private static List<InfFileStatus> infFileStatuses = new ArrayList<>();
    private static int addIndex = 0;

    /**
     * 文件ID
     */
    //@TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 文件名
     */
    //@TableField("FILE_NAME")
    private String fileName;

    /**
     * 文件长
     */
    //@TableField("LENGTH")
    private Integer length;

    /**
     * 文件内容
     */
    //@TableField("FILE_CONTENT")
    private byte[] fileContent;

    /**
     * 源系统 代码
     */
    //@TableField("FROM_SYSTEM")
    private Integer fromSystem;

    /**
     * 目标系统 代码
     */
    //@TableField("TO_SYSTEM")
    private Integer toSystem;

    /**
     * 源协议 如FEP RECP
     */
    //@TableField("FROM_PROTO")
    private String fromProto;

    /**
     * 目标协议 如FEP RECP
     */
    //@TableField("TO_PROTO")
    private String toProto;

    /**
     * 是否接收完成0:否 1:是
     */
    //@TableField("REC_FINISH")
    private Integer recFinish;

    /**
     * 是否发送完成0:否 1:是
     */
    //@TableField("SEND_FINISH")
    private Integer sendFinish;

    /**
     * 重传次数（发送）
     */
    //@TableField("TRANS_TIMES")
    private Integer transTimes;

    /**
     * 创建时间
     */
    //@TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 更新时间
     */
    //@TableField("UPDATE_TIME")
    private Date updateTime;

    /**
     * 保存路径
     */
    private String path;

    /**
     * 备注
     */
    //@TableField("REMARK")
    private String remark;
    public static InfFileStatus findById(Integer id){
        for (InfFileStatus file : infFileStatuses){
            if(file.getId() == id)
                return file;
        }
        return null;
    }

    public static InfFileStatus getByFileName(String fileName){
        for (InfFileStatus file : infFileStatuses){
            if (file.getFileName()!=null && file.getFileName().equals(fileName.trim()))
                return file;
        }
        return null;
    }

    public static boolean addInfFile(InfFileStatus fileStatus){
        addIndex++;
        fileStatus.setId(addIndex);
        infFileStatuses.add(fileStatus);
        return true;
    }

    public static boolean remove(Integer id){
        for (InfFileStatus file : infFileStatuses){
            if (file.getId() == id) {
                infFileStatuses.remove(file);
                return true;
            }
        }
        return false;
    }

    public static boolean saveFileInNewname(InfFileStatus fileStatus, String newFileName){
        boolean issuccess = true;
        File dirpath = new File(fileStatus.getPath());
        if(!dirpath.exists())
            FileUtils.mkdirs(dirpath);
        File binFile = new File(fileStatus.getPath()+"/"+newFileName==null?fileStatus.getFileName():newFileName);
        FileOutputStream out = null;
        try {
            if (!binFile.exists())
                binFile.createNewFile();
             out = new FileOutputStream(binFile);
             out.write(fileStatus.getFileContent()==null?new byte[0]:fileStatus.getFileContent());
        }catch (IOException e){
            issuccess = false;
            log.debug("文件id:{},name:{},创建失败",fileStatus.getId(), fileStatus.getFileName());
        }finally {
            if(out!=null) {
                try {
                    out.close();
                }catch (IOException e1){
                    log.debug("输出IO流关闭失败");
                }
            }
        }
        return issuccess;
    }


    public static void main(String[] args) throws IOException {
        String path = "../pathtest/";
        FileUtils.mkdirs(path);
        String filename = "test.txt";
        File newfile = new File(path+"/"+filename);
        if(!newfile.exists())
            newfile.createNewFile();
    }

}
