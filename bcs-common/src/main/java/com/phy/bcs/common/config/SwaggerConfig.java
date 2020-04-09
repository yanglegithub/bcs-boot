package com.phy.bcs.common.config;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 配置Swagger （api在线文档)
 * @author lijie
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${swagger.api.title:#{\"bcs API\"}}")
    private String title;

    @Value("${swagger.api.description:#{null}}")
    private String description;

    @Value("${swagger.api.termsOfServiceUrl:#{null}}")
    private String termsOfServiceUrl;

    @Value("${swagger.api.version:#{null}}")
    private String version;

    @Value("${swagger.api.controller.basepackage:#{\"com.phy.bcs\"}}")
    private String basePackage;

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                //配置鉴权信息
                .build()
//                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                .apiInfo(metaData());
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder().title(title).description(description).termsOfServiceUrl(termsOfServiceUrl)
                .version(version).build();
    }


    private List<ApiKey> securitySchemes() {
        return new ArrayList(Collections.singleton(new ApiKey("Authorization", "Authorization", "header")));
    }

    private List<SecurityContext> securityContexts() {
        return new ArrayList(
                Collections.singleton(SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(PathSelectors.regex("^(?!auth).*$"))
                        .build())
        );
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return new ArrayList(Collections.singleton(new SecurityReference("Authorization", authorizationScopes)));
    }
}
