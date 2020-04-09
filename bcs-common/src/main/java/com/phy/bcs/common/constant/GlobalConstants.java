package com.phy.bcs.common.constant;

/**
 * 全局常量
 */
public class GlobalConstants {

    /**
     * The constant FILE_MAX_SIZE.
     */
    public static final long FILE_MAX_SIZE = 5 * 1024 * 1024;
    public static final String UNKNOWN = "unknown";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";


    public static final String LOCALHOST_IP = "127.0.0.1";
    public static final String LOCALHOST_IP_16 = "0:0:0:0:0:0:0:1";
    public static final int MAX_IP_LENGTH = 15;

    public static final String DEV_PROFILE = "dev";
    public static final String TEST_PROFILE = "test";
    public static final String PROD_PROFILE = "prod";
    public static final int TWO_INT = 2;
    public static final int M_SIZE = 1024;
    public static final String ROOT_PREFIX = "";

    public static final int EXCEPTION_CAUSE_MAX_LENGTH = 2048;
    public static final int EXCEPTION_MESSAGE_MAX_LENGTH = 2048;
    public static final String SYSTEM_ACCOUNT = "1";
    //Regex for acceptable logins
    public static final String ACCESS_TOKEN     = "access_token_";
    public static final String ACCESS_USERID    = "access_userid_";
    public static final String ENTITY_ID_REGEX = "^[,_'.@A-Za-z0-9-]*$";
    public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";
    public static final String INDEX_URL = "index";
    public static final String AUTHENTICATED = "authenticated";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
    public static final String URL_CHECKBY = "checkBy";
    public static final String URL_FIND = "find";
    /*** 返回消息状态头 type */
    public static final String MSG_TYPE = "status";
    /*** 返回消息内容头 msg */
    public static final String MSG = "message";
    /*** 返回消息类型 info */
    public static final Integer MSG_TYPE_INFO = 0;
    /*** 返回消息类型 success */
    public static final Integer MSG_TYPE_SUCCESS = 1;
    /*** 返回消息类型 warning */
    public static final Integer MSG_TYPE_WARNING = 2;
    /*** 返回消息类型 error */
    public static final Integer MSG_TYPE_ERROR = -1;
    public enum StatusEmun {
        /**
         * 接口返回消息类型枚举
         */
        MSG_TYPE_INFO(0),
        MSG_TYPE_SUCCESS(1),
        MSG_TYPE_WARNING(2),
        MSG_TYPE_ERROR(-1);

        private int status;

        StatusEmun(int status) {
            this.status = status;
        }

        public static StatusEmun valueOf(int value) {
            StatusEmun state = null;
            if (state == null) {
                for (StatusEmun enumObj : values()) {
                    if (value == enumObj.status) {
                        return enumObj;
                    }
                }
            }
            return state;
        }

        public int getStatus() {
            return status;
        }

    }
    public interface Number {
        int THOUSAND_INT = 1000;
        int HUNDRED_INT = 100;
        int ONE_INT = 1;
        int TWO_INT = 2;
        int THREE_INT = 3;
        int FOUR_INT = 4;
        int FIVE_INT = 5;
        int SIX_INT = 6;
        int SEVEN_INT = 7;
        int EIGHT_INT = 8;
        int NINE_INT = 9;
        int TEN_INT = 10;
        int EIGHTEEN_INT = 18;
    }

    /**
     * The class Symbol.
     */
    public static final class Symbol {
        /**
         * The constant COMMA.
         */
        public static final String COMMA = ",";
        public static final String SPOT = ".";
        /**
         * The constant UNDER_LINE.
         */
        public final static String UNDER_LINE = "_";
        /**
         * The constant PER_CENT.
         */
        public final static String PER_CENT = "%";
        /**
         * The constant AT.
         */
        public final static String AT = "@";
        /**
         * The constant PIPE.
         */
        public final static String PIPE = "||";
        public final static String SHORT_LINE = "-";
        public final static String SPACE = " ";
        public static final String SLASH = "/";
        public static final String MH = ":";
        private Symbol() {
        }

    }


}
