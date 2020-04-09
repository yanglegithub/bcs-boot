package com.phy.bcs.common.util;


import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化转换
 *
 * @author lijie
 */
@Component
public class LocaleMessageUtils {

    private static final ThreadLocal<Locale> localeThreadLocal = new ThreadLocal<>();

    private static MessageSource messageSource;

    public LocaleMessageUtils(MessageSource messageSource) {
        LocaleMessageUtils.messageSource = messageSource;
    }

    public static String get(String msgKey) {
        return get(msgKey, null, LocaleContextHolder.getLocale());
    }

    public static String get(String msgKey, String defaultMsg) {
        return get(msgKey, defaultMsg, null);
    }

    public static String get(String msgKey, Object[] args) {
        return get(msgKey, null, args);
    }

    public static String get(String msgKey, String defaultMsg, Object[] args) {
        try {
            return messageSource.getMessage(msgKey, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(defaultMsg)) {
                return defaultMsg;
            } else {
                return msgKey;
            }
        }
    }

    public static String get(String msgKey, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(msgKey, args, locale);
        } catch (Exception e) {
            return msgKey;
        }
    }

    public static String get(String msgKey, String defaultMsg, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(msgKey, args, locale);
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(defaultMsg)) {
                return defaultMsg;
            } else {
                return msgKey;
            }
        }
    }

    public static Locale getCurrentLocale() {
        Locale locale = localeThreadLocal.get();
        if (locale == null) {
            //默认简体中文
            locale = Locale.CHINA;
        }
        return locale;
    }

    public static void setCurrentLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
        localeThreadLocal.set(locale);
    }


}
