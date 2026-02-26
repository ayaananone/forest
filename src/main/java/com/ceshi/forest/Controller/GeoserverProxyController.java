package com.ceshi.forest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Geoserver代理控制器
 * 用于转发WMS/WFS请求，解决跨域问题
 */
@RestController
@RequestMapping("/geoserver")
public class GeoserverProxyController {

    private static final Logger logger = LoggerFactory.getLogger(GeoserverProxyController.class);

    @Value("${geoserver.url:http://localhost:8080/geoserver}")
    private String geoserverUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/wms")
    public ResponseEntity<byte[]> proxyWms(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String url = geoserverUrl + "/wms" + (queryString != null ? "?" + queryString : "");

        logger.info("WMS代理请求: {}", url);

        try {
            logger.debug("转发WMS请求到: {}", url);
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            logger.info("WMS代理响应成功, 状态码: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (Exception e) {
            logger.error("WMS代理请求失败: {}, 错误: {}", url, e.getMessage(), e);
            return ResponseEntity.status(500).body(("WMS代理错误: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/wfs")
    public ResponseEntity<String> proxyWfs(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String url = geoserverUrl + "/wfs" + (queryString != null ? "?" + queryString : "");

        logger.info("WFS代理请求: {}", url);

        try {
            logger.debug("转发WFS请求到: {}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("WFS代理响应成功, 状态码: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .contentType(response.getHeaders().getContentType())
                    .body(response.getBody());
        } catch (Exception e) {
            logger.error("WFS代理请求失败: {}, 错误: {}", url, e.getMessage(), e);
            return ResponseEntity.status(500).body("WFS代理错误: " + e.getMessage());
        }
    }
}