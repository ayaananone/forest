package com.ceshi.forest.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * 几何工具类
 * 提供坐标转换、距离计算、几何对象创建等空间操作
 */
public class GeometryUtil {

    // WGS84坐标系（EPSG:4326）
    public static final String EPSG_4326 = "EPSG:4326";
    // Web墨卡托坐标系（EPSG:3857），用于地图显示
    public static final String EPSG_3857 = "EPSG:3857";
    // 中国常用坐标系：CGCS2000（EPSG:4490）
    public static final String EPSG_4490 = "EPSG:4490";

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 创建Point几何对象（WGS84）
     * @param longitude 经度
     * @param latitude 纬度
     * @return Point对象
     */
    public static Point createPoint(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326);
        return point;
    }

    /**
     * 创建Point几何对象（带高程）
     * @param longitude 经度
     * @param latitude 纬度
     * @param elevation 高程（米）
     * @return Point对象
     */
    public static Point createPoint(Double longitude, Double latitude, Double elevation) {
        if (longitude == null || latitude == null) {
            return null;
        }
        Coordinate coordinate = new Coordinate(longitude, latitude, elevation);
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326);
        return point;
    }

    /**
     * 计算两点之间的球面距离（使用Haversine公式）
     * @param lon1 起点经度
     * @param lat1 起点纬度
     * @param lon2 终点经度
     * @param lat2 终点纬度
     * @return 距离（米）
     */
    public static double distanceHaversine(Double lon1, Double lat1, Double lon2, Double lat2) {
        if (lon1 == null || lat1 == null || lon2 == null || lat2 == null) {
            return Double.NaN;
        }

        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 使用Geotools进行坐标转换
     * @param point 原始点
     * @param sourceEPSG 源坐标系
     * @param targetEPSG 目标坐标系
     * @return 转换后的点
     */
    public static Point transformCoordinate(Point point, String sourceEPSG, String targetEPSG) {
        if (point == null) {
            return null;
        }

        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceEPSG);
            CoordinateReferenceSystem targetCRS = CRS.decode(targetEPSG);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            Point transformedPoint = (Point) JTS.transform(point, transform);
            transformedPoint.setSRID(Integer.parseInt(targetEPSG.split(":")[1]));
            return transformedPoint;

        } catch (Exception e) {
            throw new RuntimeException("坐标转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * WGS84转Web墨卡托（用于地图显示）
     * @param lon 经度
     * @param lat 纬度
     * @return [x, y] 坐标数组
     */
    public static double[] wgs84ToWebMercator(Double lon, Double lat) {
        if (lon == null || lat == null) {
            return null;
        }

        double x = lon * 20037508.34 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;

        return new double[]{x, y};
    }

    /**
     * Web墨卡托转WGS84
     * @param x Web墨卡托X坐标
     * @param y Web墨卡托Y坐标
     * @return [lon, lat] 经纬度数组
     */
    public static double[] webMercatorToWgs84(Double x, Double y) {
        if (x == null || y == null) {
            return null;
        }

        double lon = x / 20037508.34 * 180;
        double lat = y / 20037508.34 * 180;
        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

        return new double[]{lon, lat};
    }

    /**
     * 计算矩形范围（用于地图视野查询）
     * @param centerLon 中心经度
     * @param centerLat 中心纬度
     * @param widthMeters 宽度（米）
     * @param heightMeters 高度（米）
     * @return [minLon, minLat, maxLon, maxLat]
     */
    public static double[] calculateExtent(Double centerLon, Double centerLat,
                                           Double widthMeters, Double heightMeters) {
        if (centerLon == null || centerLat == null) {
            return null;
        }

        // 粗略估算：1度纬度 ≈ 111km，1度经度 ≈ 111km * cos(纬度)
        double latDelta = (heightMeters / 2) / 111000.0;
        double lonDelta = (widthMeters / 2) / (111000.0 * Math.cos(Math.toRadians(centerLat)));

        return new double[]{
                centerLon - lonDelta,  // minLon
                centerLat - latDelta,  // minLat
                centerLon + lonDelta,  // maxLon
                centerLat + latDelta   // maxLat
        };
    }

    /**
     * 格式化坐标为字符串
     * @param lon 经度
     * @param lat 纬度
     * @param precision 小数位数
     * @return 格式化字符串
     */
    public static String formatCoordinate(Double lon, Double lat, int precision) {
        if (lon == null || lat == null) {
            return "N/A";
        }
        String format = "%.%df, %.%df".replace("%d", String.valueOf(precision));
        return String.format(format, lon, lat);
    }

    /**
     * 判断点是否在多边形内（简化版，实际使用JTS的contains）
     * @param point 点
     * @param polygonCoords 多边形顶点坐标数组
     * @return 是否在内部
     */
    public static boolean isPointInPolygon(Point point, double[][] polygonCoords) {
        if (point == null || polygonCoords == null || polygonCoords.length < 3) {
            return false;
        }

        // 使用射线法判断
        double x = point.getX();
        double y = point.getY();
        boolean inside = false;

        for (int i = 0, j = polygonCoords.length - 1; i < polygonCoords.length; j = i++) {
            double xi = polygonCoords[i][0], yi = polygonCoords[i][1];
            double xj = polygonCoords[j][0], yj = polygonCoords[j][1];

            if (((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * 生成WKT格式字符串
     * @param lon 经度
     * @param lat 纬度
     * @return WKT字符串，如 "POINT(118.7 32.1)"
     */
    public static String toWkt(Double lon, Double lat) {
        if (lon == null || lat == null) {
            return null;
        }
        return String.format("POINT(%.6f %.6f)", lon, lat);
    }

    /**
     * 解析WKT字符串
     * @param wkt WKT格式字符串
     * @return [lon, lat] 数组
     */
    public static double[] fromWkt(String wkt) {
        if (wkt == null || !wkt.startsWith("POINT")) {
            return null;
        }

        try {
            String coords = wkt.replace("POINT(", "").replace(")", "").trim();
            String[] parts = coords.split("\s+");
            return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (Exception e) {
            return null;
        }
    }
}

