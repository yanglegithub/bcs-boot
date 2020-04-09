package com.phy.bcs.common.config;

import com.phy.bcs.common.util.converter.OrikaLocalDateConverter;
import com.phy.bcs.common.util.converter.OrikaLocalDateTimeConverter;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

/**
 * Orika配置 （Bean映射工具)
 *
 * @author lijie
 */
@Component
public class OrikaConfiguration implements OrikaMapperFactoryConfigurer {

    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.getConverterFactory().registerConverter(new OrikaLocalDateTimeConverter());
        orikaMapperFactory.getConverterFactory().registerConverter(new OrikaLocalDateConverter());
    }
}
