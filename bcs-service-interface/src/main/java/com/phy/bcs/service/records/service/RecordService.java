package com.phy.bcs.service.records.service;

import com.phy.bcs.common.mvc.service.DataService;
import com.phy.bcs.service.records.mapper.RecordMapper;
import com.phy.bcs.service.records.model.Record;
import com.phy.bcs.service.records.model.vo.RecordVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yangl
 * @since 2020-04-28
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RecordService extends DataService<RecordMapper, Record, RecordVo> {

}
