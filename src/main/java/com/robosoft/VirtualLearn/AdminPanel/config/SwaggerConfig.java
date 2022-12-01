package com.robosoft.VirtualLearn.AdminPanel.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private ApiKey apiKeys(){
        return new ApiKey("JWT_TOKEN",AUTHORIZATION_HEADER,"header");
    }

    private List<SecurityContext> securityContexts(){
        return Arrays.asList(SecurityContext.builder().securityReferences(sf()).build());
    }

    private List<SecurityReference> sf(){
        AuthorizationScope scope = new AuthorizationScope("Global","accessEverything");
        return Arrays.asList(new SecurityReference("JWT_TOKEN",new AuthorizationScope[] { scope }));
    }

    @Bean
    public Docket api(){

        return new  Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getInfo()).securityContexts(securityContexts()).securitySchemes(Arrays.asList(apiKeys())).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
    }

    private ApiInfo getInfo(){
        return new ApiInfo("Virtual Learn - Backend Course","This project is developed by Robosoft java backend team","2.7.5",
                "Terms of service",
                new Contact("Virtual Learn Team","********","virtuallearn2022@gmail.com"),
                "Licence of APIs","API License URL", Collections.emptyList());
    }
}
