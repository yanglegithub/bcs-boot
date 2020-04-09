package com.phy.bcs.common.util;

import com.phy.bcs.common.mvc.domain.BaseModel;
import com.phy.bcs.common.mvc.domain.dto.BaseModelDto;
import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;
import com.phy.bcs.common.util.spring.SpringContextHolder;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;

import java.util.List;

/**
 * Bean 转换工具
 * @author lijie
 */
public class BeanConvertUtils {

    private volatile static MapperFacade mapperFacade;
    public static MapperFacade getMapperFacade(){
        if (mapperFacade == null) {
            synchronized (BeanConvertUtils.class) {
                if (mapperFacade == null) {
                    mapperFacade = SpringContextHolder.getBean(MapperFacade.class);
                }
            }
        }
        return mapperFacade;
    }

    private volatile static MapperFactory orikaMapperFactory;
    private static MapperFactory getOrikaMapperFactory(){
        if (orikaMapperFactory == null) {
            synchronized (BeanConvertUtils.class) {
                if (orikaMapperFactory == null) {
                    orikaMapperFactory = SpringContextHolder.getBean(MapperFactory.class);
                }
            }
        }
        return BeanConvertUtils.orikaMapperFactory;
    }

    public static <S, D> void copyProperties(S sourceObject, D destination) {
        getMapperFacade().map(sourceObject, destination);
    }

    public static <S, D extends BaseModelDto> void beanToDto(S sourceObject, D destination){
        getMapperFacade().map(sourceObject,destination);
    }

    public static <S, D extends BaseModelDto> D beanToDto(S sourceObject, Class<D> destinationClass){
        return  getMapperFacade().map(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModel> void dtoToBean(S sourceObject, D destination){
        getMapperFacade().map(sourceObject,destination);
    }

    public static <S, D extends BaseModel> D dtoToBean(S sourceObject, Class<D> destinationClass){
        return  getMapperFacade().map(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelDto> List<D> beanToDtoList(List<S> sourceObject, Class<D> destinationClass){
        return  getMapperFacade().mapAsList(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModel> List<D> dtoToBeanList(List<S> sourceObject, Class<D> destinationClass){
        return  getMapperFacade().mapAsList(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelVo> void beanToVo(S sourceObject, D destination){
        getMapperFacade().map(sourceObject,destination);
    }

    public static <S, D extends BaseModelVo> D beanToVo(S sourceObject, Class<D> destinationClass){
        return  getMapperFacade().map(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModel> void voToBean(S sourceObject, D destination){
        getMapperFacade().map(sourceObject,destination);
    }

    public static <S, D extends BaseModel> D voToBean(S sourceObject, Class<D> destinationClass){
        return  getMapperFacade().map(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelVo> List<D> beanToVoList(List<S> sourceObject, Class<D> destinationClass){
        return  getMapperFacade().mapAsList(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModel> List<D> voToBeanList(List<S> sourceObject, Class<D> destinationClass){
        return  getMapperFacade().mapAsList(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelDto> void voToDto(S sourceObject, D destination){
        getMapperFacade().map(sourceObject,destination);
    }

    public static <S, D extends BaseModelDto> D voToDto(S sourceObject, Class<D> destinationClass){
        return  getMapperFacade().map(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelVo> void dtoToVo(S sourceObject, D destination){
        getMapperFacade().map(sourceObject,destination);
    }

    public static <S, D extends BaseModelVo> D dtoToVo(S sourceObject, Class<D> destinationClass){
        return  getMapperFacade().map(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelDto> List<D> voToDtoList(List<S> sourceObject, Class<D> destinationClass){
        return  getMapperFacade().mapAsList(sourceObject,destinationClass);
    }

    public static <S, D extends BaseModelVo> List<D> dtoToVoList(List<S> sourceObject, Class<D> destinationClass){
        return  getMapperFacade().mapAsList(sourceObject,destinationClass);
    }
}
