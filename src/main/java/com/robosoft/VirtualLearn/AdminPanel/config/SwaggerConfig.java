package com.robosoft.VirtualLearn.AdminPanel.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api(){

        return new  Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getInfo()).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
    }

    private ApiInfo getInfo(){
        return new ApiInfo("Virtual Learn - Backend Course","This project is developed by Robosoft java backend team","2.7.5",
                "Terms of service",
                new Contact("Virtual Learn Team","********","virtuallearn2022@gmail.com"),
                "Licence of APIs","API License URL", Collections.emptyList());
    }
}
