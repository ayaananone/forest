package com.ceshi.forest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Geoserver代理控制器
 * 用于转发WMS/WFS请求，解决跨域问题
 */
@Slf4j
@RestController
@RequestMapping("/geoserver")
public class GeoserverProxyController {

    @Value("${geoserver.url:http://localhost:8080/geoserver}")
    private String geoserverUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/wms")
    public ResponseEntity<byte[]> proxyWms(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String url = geoserverUrl + "/wms" + (queryString != null ? "?" + queryString : "");

        log.info("WMS代理请求: {}", url);

        try {
            log.debug("转发WMS请求到: {}", url);
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            log.info("WMS代理响应成功, 状态码: {}, Content-Type: {}",
                    response.getStatusCode(),
                    response.getHeaders().getContentType());

            HttpHeaders headers = buildHeaders(response.getHeaders(), MediaType.IMAGE_PNG);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(headers)
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("WMS代理请求失败: {}, 错误: {}", url, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("WMS代理错误: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/wfs")
    public ResponseEntity<String> proxyWfs(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String url = geoserverUrl + "/wfs" + (queryString != null ? "?" + queryString : "");

        log.info("WFS代理请求: {}", url);

        try {
            log.debug("转发WFS请求到: {}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            log.info("WFS代理响应成功, 状态码: {}", response.getStatusCode());

            HttpHeaders headers = buildHeaders(response.getHeaders(), MediaType.APPLICATION_XML);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(headers)
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("WFS代理请求失败: {}, 错误: {}", url, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("WFS代理错误: " + e.getMessage());
        }
    }

    /**
     * 构建响应头
     * @param sourceHeaders 原始响应头
     * @param defaultContentType 默认Content-Type（当原始头中不存在时）
     * @return 构建好的HttpHeaders
     */
    private HttpHeaders buildHeaders(org.springframework.http.HttpHeaders sourceHeaders,
                                     MediaType defaultContentType) {
        HttpHeaders headers = new HttpHeaders();

        // 设置 Content-Type
        if (sourceHeaders.getContentType() != null) {
            headers.setContentType(sourceHeaders.getContentType());
        } else {
            headers.setContentType(defaultContentType);
        }

        // 设置 Content-Length
        if (sourceHeaders.getContentLength() > 0) {
            headers.setContentLength(sourceHeaders.getContentLength());
        }

        // 设置 Cache-Control
        if (sourceHeaders.getCacheControl() != null) {
            headers.setCacheControl(sourceHeaders.getCacheControl());
        }

        // 添加 CORS 头
        headers.setAccessControlAllowOrigin("*");
        headers.setAccessControlAllowMethods(java.util.List.of(
                org.springframework.http.HttpMethod.GET,
                org.springframework.http.HttpMethod.POST,
                org.springframework.http.HttpMethod.OPTIONS
        ));

        return headers;
    }
}