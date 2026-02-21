package com.ceshi.forest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧林场GIS平台 - 主启动类
 *
 * 技术栈：
 * - Spring Boot 2.7
 * - PostgreSQL + PostGIS
 * - Hibernate Spatial
 * - Geoserver（地图服务）
 * - OpenLayers（前端地图）
 */
@SpringBootApplication
public class ForestGisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForestGisApplication.class, args);
        System.out.println("========================================");
        System.out.println("  智慧林场GIS平台启动成功！");
        System.out.println("========================================");
        System.out.println("API文档: http://localhost:8081/api/stands/health");
        System.out.println("地图服务: http://localhost:8081/geoserver/wms");
        System.out.println("========================================");
    }
}