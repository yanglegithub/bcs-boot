package com.phy.bcs.common.config;

import com.phy.bcs.common.interceptor.OperateInterceptor;
import com.phy.bcs.common.security.constant.SecurityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 */
@Configuration
public class WebBaseConfiguration implements WebMvcConfigurer {

    @Bean
    public javax.validation.Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * 拦截器配置
     * @param registry
     */
    @Override
    @Profile("!prod")
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OperateInterceptor()).addPathPatterns("/**")
            .excludePathPatterns("*.js", "/**/*.js", "/**/*.png", "*.css", "/**/*.css", "*.html", "/**/*.html")
            .excludePathPatterns(SecurityConstants.authorizePermitAll);
    }

    /**
     * 跨域配置
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(corsConfigurationSource);
    }


}
