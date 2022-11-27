package com.robosoft.VirtualLearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class VirtualLearnApplication {
	public static void main(String[] args) {
		SpringApplication.run(VirtualLearnApplication.class, args);

	}


}
