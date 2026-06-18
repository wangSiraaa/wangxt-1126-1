package com.heatstation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.heatstation.mapper")
public class HeatStationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeatStationApplication.class, args);
    }
}
