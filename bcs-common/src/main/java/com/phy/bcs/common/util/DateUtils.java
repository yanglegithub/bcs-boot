package com.phy.bcs.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间工具类
 */
@Slf4j
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    /*public static final TimeZone SYSTEM_TIME_ZONE = TimeZone.getTimeZone(SystemConfig.get("system.timeZone"));
    public static final TimeZone FRONT_TRANSFER_TIME_ZONE = TimeZone.getTimeZone(SystemConfig.get("system.frontTransferTimeZone"));*/

    public static final String T_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String INTERFACE_FILE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String INTERFACE_FILE_PATTERN_MILLI = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private static String[] parsePatterns = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.s", "yyyy-MM-dd HH:mm", "yyyy", "yyyyMM", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd",
        "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"};

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd）
     */
    public static String getDate() {
        return getDate("yyyy-MM-dd");
    }

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    public static String getDate(String pattern, TimeZone timeZone) {
        return DateFormatUtils.format(new Date(), pattern, timeZone);
    }

    /**
     * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, Object... pattern) {
        String formatDate = null;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    public static String formatDate(Date date, TimeZone timeZone, Object... pattern) {
        String formatDate = null;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString(), timeZone);
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd", timeZone);
        }
        return formatDate;
    }

    /**
     * 得到年月时间字符串，转换格式（yyyy-MM）
     */
    public static String getYearAndMonth(Date date) {
        return formatDate(date, "yyyy-MM");
    }

    /**
     * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前时间字符串 格式（HH:mm:ss）
     */
    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    /**
     * 得到当前日期和时间字符串 格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前年份字符串 格式（yyyy）
     */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /**
     * 得到当前月份字符串 格式（MM）
     */
    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    /**
     * 得到当天字符串 格式（dd）
     */
    public static String getDay() {
        return formatDate(new Date(), "dd");
    }

    /**
     * 得到当前星期字符串 格式（E）星期几
     */
    public static String getWeek() {
        return formatDate(new Date(), "E");
    }

    /**
     * 日期型字符串转化为日期 格式 { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }


    public static Date parseDate(Object str, String format) {
        if (str == null) {
            return null;
        }
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(format);
            return fmt.parse(str.toString());
        } catch (Exception e) {
            log.warn("时间转换失败", e);
            return null;
        }
    }

    public static Date parseInterfaceFileDate(String str, final TimeZone timeZone) {
        if (str == null) {
            return null;
        }
        String pattern;
        if (str.indexOf(".") == -1) {
            pattern = INTERFACE_FILE_PATTERN;
        } else {
            pattern = INTERFACE_FILE_PATTERN_MILLI;
        }
        return parseDate(str, pattern, timeZone);
    }

    /**
     * 通过格式，时区转换时间
     */
    public static Date parseDate(Object str, String pattern, final TimeZone timeZone) {
        if (str == null) {
            return null;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(timeZone);
        try {
            return sdf.parse(str.toString());
        } catch (ParseException e) {
            log.warn("时间转换失败", e);
            return null;
        }
    }

    /**
     * 时间转换，异常直接抛出
     * */
    public static Date parseDateWithException(Object str, String pattern, final TimeZone timeZone) throws ParseException {
        if (str == null) {
            return null;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(timeZone);
        return sdf.parse(str.toString());
    }

    /**
     * 将"yyyyMMddHHmmss"转换为"yyyy.MM.dd HH:mm:ss"
     */
    public static String parseString(String str) {
        if (str == null) {
            return null;
        }
        try {
            char[] strArray = str.toCharArray();
            String newStr = "";
            for (int i = 0; i < strArray.length; i++) {
                if (i == 4 || i == 6) {
                    newStr = newStr + ".";
                } else if (i == 8) {
                    newStr = newStr + " ";
                } else if (i == 10 || i == 12) {
                    newStr = newStr + ":";
                }
                newStr = newStr + strArray[i];
            }
            return newStr;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

    }

    /**
     * 获取过去的天数
     *
     * @param date
     * @return
     */
    public static long pastDays(Date date) {
        long t = System.currentTimeMillis() - date.getTime();
        return t / (24 * 60 * 60 * 1000);
    }

    /**
     * 获取过去的小时
     *
     * @param date
     * @return
     */
    public static long pastHour(Date date) {
        long t = System.currentTimeMillis() - date.getTime();
        return t / (60 * 60 * 1000);
    }

    /**
     * 获取过去的分钟
     *
     * @param date
     * @return
     */
    public static long pastMinutes(Date date) {
        long t = System.currentTimeMillis() - date.getTime();
        return t / (60 * 1000);
    }

    /**
     * 转换为时间（天,时:分:秒.毫秒）
     *
     * @param timeMillis
     * @return
     */
    public static String formatDateTime(long timeMillis) {
        long day = timeMillis / (24 * 60 * 60 * 1000);
        long hour = (timeMillis / (60 * 60 * 1000) - day * 24);
        long min = ((timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        long sss = (timeMillis - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000 - min * 60 * 1000 - s * 1000);
        return (day > 0 ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
    }

    /**
     * 获取两个日期之间的天数
     *
     * @param before
     * @param after
     * @return
     */
    public static double getDistanceOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
    }

    /**
     * 取得时间
     *
     * @param date
     * @param format
     * @return 20058-12-25
     */
    public static Date parseDate(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date NDate = null;
        try {
            NDate = formatter.parse(formatDate(date, format));
        } catch (ParseException e) {
            log.warn("时间转换失败", e);
        }
        return NDate;
    }

    public static Date clearMillisecond(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取时间错误", e);
        }
        return null;
    }

    // 01. java.util.Date --> java.time.LocalDateTime
    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 取得当月天数
     */
    public static int getCurrentMonthLastDay() {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);//把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取指定日期当月的第一天
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Date getFirstDayOfGivenMonth(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 0);
            return calendar.getTime();
        } catch (ParseException e) {
            log.warn("时间转换错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期当月的第一天的开始时间
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfGivenMonth(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MONTH, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定日期当月的第一天的开始时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期下个月的第一天
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Date getFirstDayOfNextMonth(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return calendar.getTime();
        } catch (ParseException e) {
            log.warn("获取指定日期下个月的第一天错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期下个月的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfNextMonth(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期的下一天0点时间
     *
     * @return
     */
    public static Date getNextDayStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的前一天0点时间
     *
     * @return
     */
    public static Date getLastHoursStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getLastHoursEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的前一天0点时间
     *
     * @return
     */
    public static Date getLastDayStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getLastDayStartTime(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的当天0点时间
     *
     * @return
     */
    public static Date getGivenDayStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getGivenDayStartTime(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getGivenDayEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    public static Date getGivenDayEndTime(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    /**
     * 周一作为第一天
     */
    public static Date getGivenWeekStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getGivenWeekStartTime(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 周日作为最后一天
     */
    public static Date getGivenWeekEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    public static Date getGivenWeekEndTime(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    /**
     * 获取指定日期当月开始时间
     *
     * @return
     */
    public static Date getGivenMonthStartTime(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static Date getGivenMonthStartTime(Date date, TimeZone timeZone) {
        try {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期当月最后一天24点时间
     *
     * @return
     */
    public static Date getGivenMonthEndTime(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static Date getGivenMonthEndTime(Date date, TimeZone timeZone) {
        try {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期当季度开始时间
     *
     * @return
     */
    public static Date getGivenQuarterStartTime(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int quarterIndex = calendar.get(Calendar.MONTH) / 3;
            calendar.set(Calendar.MONTH, quarterIndex * 3);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static Date getGivenQuarterStartTime(Date date, TimeZone timeZone) {
        try {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(date);

            int quarterIndex = calendar.get(Calendar.MONTH) / 3;
            calendar.set(Calendar.MONTH, quarterIndex * 3);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期当季度最后一天24点时间
     *
     * @return
     */
    public static Date getGivenQuarterEndTime(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int quarterIndex = calendar.get(Calendar.MONTH) / 3;
            calendar.set(Calendar.MONTH, quarterIndex * 3 + 2);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static Date getGivenQuarterEndTime(Date date, TimeZone timeZone) {
        try {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(date);

            int quarterIndex = calendar.get(Calendar.MONTH) / 3;
            calendar.set(Calendar.MONTH, quarterIndex * 3 + 2);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期当年第一天0点时间
     *
     * @return
     */
    public static Date getGivenYearStartTime(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static Date getGivenYearStartTime(Date date, TimeZone timeZone) {
        try {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(date);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    /**
     * 获取指定日期当年最后一天24点时间
     *
     * @return
     */
    public static Date getGivenYearEndTime(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.YEAR, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static Date getGivenYearEndTime(Date date, TimeZone timeZone) {
        try {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(date);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.YEAR, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            return calendar.getTime();
        } catch (Exception e) {
            log.warn("获取指定时间错误", e);
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("转换接口时间类型 default ：" + parseInterfaceFileDate("2019-11-07T16:56:15.005", TimeZoneEnum.CN.value()).toInstant());
        /* System.out.println("本年最后时间 default ：" + getGivenYearEndTime(PublicUtils.getCurrentDate()).toInstant());
        System.out.println("本年最后时间 UTC：" + getGivenYearEndTime(PublicUtils.getCurrentDate(), TimeZoneEnum.UTC.value()).toInstant());
        System.out.println("本年最后时间 CN：" + getGivenYearEndTime(PublicUtils.getCurrentDate(), TimeZoneEnum.CN.value()).toInstant());

     */

    }
}



