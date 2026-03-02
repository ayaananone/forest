package com.ceshi.forest.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.sql.*;

@MappedTypes({Geometry.class, Point.class})
public class PostGISGeometryTypeHandler extends BaseTypeHandler<Geometry> {

    private static final ThreadLocal<WKBReader> wkbReader = ThreadLocal.withInitial(WKBReader::new);
    private static final ThreadLocal<WKBWriter> wkbWriter = ThreadLocal.withInitial(WKBWriter::new);
    private static final ThreadLocal<WKTReader> wktReader = ThreadLocal.withInitial(WKTReader::new);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType) throws SQLException {
        // 使用 WKT 格式插入，更可靠
        String wkt = "SRID=4326;" + parameter.toText();
        ps.setObject(i, wkt);
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return parseGeometry(obj);
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return parseGeometry(obj);
    }

    @Override
    public Geometry getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return parseGeometry(obj);
    }

    private Geometry parseGeometry(Object obj) {
        if (obj == null) {
            return null;
        }

        // 处理 PGobject (PostgreSQL JDBC 驱动的默认返回类型)
        if (obj.getClass().getName().equals("org.postgresql.util.PGobject")) {
            try {
                // 通过反射获取 value 字段
                java.lang.reflect.Field valueField = obj.getClass().getDeclaredField("value");
                valueField.setAccessible(true);
                String value = (String) valueField.get(obj);

                // 或者使用 getValue() 方法（如果存在）
                // String value = (String) obj.getClass().getMethod("getValue").invoke(obj);

                if (value != null) {
                    // EWKT 格式: SRID=4326;POINT(...)
                    if (value.startsWith("SRID=")) {
                        int idx = value.indexOf(';');
                        if (idx > 0) {
                            value = value.substring(idx + 1);
                        }
                    }
                    return wktReader.get().read(value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse PGobject", e);
            }
        }

        // 如果是 String (WKT 格式)
        if (obj instanceof String) {
            try {
                String wkt = (String) obj;
                // 处理 EWKT 前缀
                if (wkt.startsWith("SRID=")) {
                    int idx = wkt.indexOf(';');
                    if (idx > 0) {
                        wkt = wkt.substring(idx + 1);
                    }
                }
                return wktReader.get().read(wkt);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse WKT: " + obj, e);
            }
        }

        // 如果是 byte[] (WKB 格式)
        if (obj instanceof byte[]) {
            try {
                return wkbReader.get().read((byte[]) obj);
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse WKB", e);
            }
        }

        throw new RuntimeException("Unsupported geometry type: " + obj.getClass());
    }
}