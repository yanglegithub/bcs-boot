package com.phy.bcs.common.util.converter;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalDateTime 转换器
 *
 * @author lijie
 */
public class OrikaLocalDateTimeConverter extends BidirectionalConverter<Date, LocalDateTime> {

    @Override
    public LocalDateTime convertTo(Date source, Type<LocalDateTime> destinationType, MappingContext mappingContext) {
        Instant instant = source.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime res = LocalDateTime.ofInstant(instant, zone);
        return res;
    }

    @Override
    public Date convertFrom(LocalDateTime source, Type<Date> destinationType, MappingContext mappingContext) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = source.atZone(zone).toInstant();
        Date res = Date.from(instant);
        return res;

    }
}
