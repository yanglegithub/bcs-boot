package com.phy.bcs.common.config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * 设置默认时区，服务端数据统一采用UTC时间
 *
 * @author lijie
 */
@Configuration
public class TimeZoneConfiguration {

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
