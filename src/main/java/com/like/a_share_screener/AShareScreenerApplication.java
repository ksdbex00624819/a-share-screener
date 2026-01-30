package com.like.a_share_screener;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.like.a_share_screener.persistence.mapper")
@ConfigurationPropertiesScan
public class AShareScreenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AShareScreenerApplication.class, args);
	}

}
