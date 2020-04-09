package com.phy.bcs.common.security.constant;

public class SecurityConstants {

    public static final String BEARER_TOKEN_TYPE = "Basic ";
    public static final String DEFAULT_PASSWORD = "111111";
    public static final String ADMIN_LOGIN_ID = "admin";
    public static final String ANONYMOUS_USER_ID = "ANONYMOUS";
    public static String[] authorizePermitAll = {"/", "/error", "/**/actuator/**", "/**/swagger-resources/**",
            "/**/swagger-ui*", "/**/webjars/springfox-swagger-ui/**", "/**/v2/api-docs/**", "/management/**"};
    public static String USER_LOGIN_ID_IN_HEADER = "USER_LOGIN_ID";
}
