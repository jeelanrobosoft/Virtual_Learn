package com.robosoft.VirtualLearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;


@SpringBootApplication
@EnableScheduling
@EnableWebMvc
public class VirtualLearnApplication extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(VirtualLearnApplication.class, args);
	}


	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.defaultContentType(MediaType.APPLICATION_JSON);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/***").allowedOrigins("http://localhost:3000").allowedHeaders("*").allowedMethods("GET","PUT","POST")
				.exposedHeaders("jwt-token","refreshToken");
	}

//	@Bean
//	public WebMvcConfigurer corsConfigurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/***").allowedOrigins("http://localhost:3000").allowedHeaders("*").allowedMethods("GET","PUT","POST");
//			}
//		};
//	}



}
