package com.ceshi.forest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧林场GIS平台 - 主启动类
 */
@SpringBootApplication
@MapperScan("com.ceshi.forest.mapper")  // MyBatis Mapper 扫描路径
public class ForestGisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForestGisApplication.class, args);
        System.out.println("========================================");
        System.out.println("  智慧林场GIS平台启动成功！");
        System.out.println("========================================");
        System.out.println("API文档: http://localhost:8081/api/stands/health");
        System.out.println("地图服务: http://localhost:8080/geoserver/wms");
        System.out.println("========================================");
    }
}