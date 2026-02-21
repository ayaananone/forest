package com.ceshi.forest.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Geoserver代理控制器
 * 用于转发WMS/WFS请求，解决跨域问题
 */
@RestController
@RequestMapping("/geoserver")
public class GeoserverProxyController {

    @Value("${geoserver.url:http://localhost:8080/geoserver}")
    private String geoserverUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 代理WMS请求
    @GetMapping("/wms")
    public ResponseEntity<byte[]> proxyWms(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String url = geoserverUrl + "/wms" + (queryString != null ? "?" + queryString : "");

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage().getBytes());
        }
    }

    // 代理WFS请求
    @GetMapping("/wfs")
    public ResponseEntity<String> proxyWfs(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String url = geoserverUrl + "/wfs" + (queryString != null ? "?" + queryString : "");

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .contentType(response.getHeaders().getContentType())
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}