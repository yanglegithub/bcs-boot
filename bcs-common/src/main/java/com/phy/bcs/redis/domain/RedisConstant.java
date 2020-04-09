package com.phy.bcs.redis.domain;

/**
 * Redis常量
 * 命名规范：key:-存储对象实体所在模块名-存储对象名称
 * 如果是hash存储，item：标识名称_唯一性标识
 *
 */
public class RedisConstant {
    /**
     * 认证key前缀
     */
    public static final String AUTH_PREFIX = "oauth:";

}
