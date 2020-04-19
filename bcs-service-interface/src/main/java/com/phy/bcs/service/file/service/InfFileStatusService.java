//package com.phy.bcs.service.file.service;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.phy.bcs.common.mvc.service.DataService;
//import com.phy.bcs.service.file.mapper.InfFileStatusMapper;
//import com.phy.bcs.service.file.model.InfFileStatus;
//import com.phy.bcs.service.file.model.vo.InfFileStatusVo;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
///**
// * <p>
// *  服务类
// * </p>
// *
// * @author yangl
// * @since 2020-04-02
// */
//@Service
//@Transactional(rollbackFor = Exception.class)
//public class InfFileStatusService extends DataService<InfFileStatusMapper, InfFileStatus, InfFileStatusVo>  {
//
//    /**
//     * 跟据文件名查找还没有完成接收的文件
//     * @param filename
//     * @return
//     */
//    public InfFileStatus findInByFilename(String filename){
//        QueryWrapper<InfFileStatus> query = new QueryWrapper<InfFileStatus>()
//                .eq("REC_FINIDH", 0)
//                .eq("FILE_NAME", filename.trim());
//        InfFileStatus fileStatus = null;
//        try{
//            fileStatus = baseMapper.selectOne(query);
//        }catch (Exception e){}
//
//        return fileStatus;
//    }
//
//    /**
//     * 跟据文件名查找文件
//     * @param filename
//     * @return
//     */
//    public InfFileStatus findOneByFilename(String filename){
//        QueryWrapper<InfFileStatus> query = new QueryWrapper<InfFileStatus>()
//                .eq("FILE_NAME", filename.trim());
//        InfFileStatus fileStatus = null;
//        try{
//            fileStatus = baseMapper.selectOne(query);
//        }catch (Exception e){}
//
//        return fileStatus;
//    }
//
//    public List<InfFileStatus> findAllObject(){
//        return baseMapper.findAllObject();
//    }
//
//    public List<InfFileStatus> findAllSendFiles(){
//        QueryWrapper<InfFileStatus> query = new QueryWrapper<InfFileStatus>()
//                .eq("REC_FINISH", 1)
//                .eq("SEND_FINISH", 0)
//                .eq("TO_PROTO", "RECP");
//        return baseMapper.selectList(query);
//    }
//}
