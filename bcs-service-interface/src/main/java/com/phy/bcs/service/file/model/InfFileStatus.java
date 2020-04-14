package com.phy.bcs.service.file.model;

import com.phy.bcs.common.mvc.domain.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
     * 备注
     */
    //@TableField("REMARK")
    private String remark;

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

    /*public static void main(String[] args){
        InfFileStatus file = new InfFileStatus();
        file.setFileName("hello");
        InfFileStatus file1 = new InfFileStatus();
        InfFileStatus file2 = new InfFileStatus();
        InfFileStatus file3 = new InfFileStatus();
        InfFileStatus.addInfFile(file);
        InfFileStatus.addInfFile(file1);
        InfFileStatus.addInfFile(file2);
        InfFileStatus.addInfFile(file3);
        InfFileStatus.remove(2);
        if(true){
            InfFileStatus file5 = InfFileStatus.getByFileName("hello");
            file5.setFileName("hello,world");
        }
        System.out.println("end");
    }*/

}
