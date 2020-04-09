package com.phy.bcs.common.mvc.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phy.bcs.common.mvc.domain.BaseModel;

/**
 * DAO支持类
 * @author lijie
 */
public interface BaseRepository<T extends BaseModel> extends BaseMapper<T> {

}
