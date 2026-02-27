package com.ceshi.forest.util;

import com.ceshi.forest.dto.TreeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据导出工具类
 * 支持 CSV、Excel、JSON 格式导出
 */
@Component
public class ExportUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExportUtil.class);

    // CSV 表头（按数据库字段顺序）
    private static final String[] CSV_HEADERS = {
            "单木编号", "样地编号", "树木编号", "树种", "平均胸径(cm)",
            "树高(m)", "1/2高处直径(cm)", "Q2", "F1", "断面积(m²)",
            "材积(m³)", "冠幅(m)", "胸径1(cm)", "胸径2(cm)", "健康状况",
            "树种代码", "调查日期", "木材质量", "林分编号"
    };

    // Excel 表头
    private static final String[] EXCEL_HEADERS = CSV_HEADERS;

    /**
     * 导出为 CSV 格式
     */
    public byte[] exportToCsv(List<TreeDTO> trees, Integer standId) {
        logger.info("开始导出 CSV 格式，林分ID: {}，记录数: {}", standId, trees.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // 写入 BOM
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // 写入表头
            writer.println(String.join(",", CSV_HEADERS));

            // 写入数据行
            for (TreeDTO tree : trees) {
                String[] row = convertToStringArray(tree);
                // 处理包含逗号的字段
                for (int i = 0; i < row.length; i++) {
                    if (row[i] != null && (row[i].contains(",") || row[i].contains("\"") || row[i].contains("\n"))) {
                        row[i] = "\"" + row[i].replace("\"", "\"\"") + "\"";
                    }
                }
                writer.println(String.join(",", row));
            }

            writer.flush();
            logger.info("CSV 导出完成，大小: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("CSV 导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("CSV 导出失败", e);
        }
    }

    /**
     * 导出为 Excel 格式 (xlsx)
     */
    public byte[] exportToExcel(List<TreeDTO> trees, Integer standId) {
        logger.info("开始导出 Excel 格式，林分ID: {}，记录数: {}", standId, trees.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("单木数据");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXCEL_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXCEL_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入数据
            int rowNum = 1;
            for (TreeDTO tree : trees) {
                Row row = sheet.createRow(rowNum++);
                fillExcelRow(row, tree, dataStyle, numberStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < EXCEL_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 4 * 256);
            }

            workbook.write(baos);
            logger.info("Excel 导出完成，大小: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Excel 导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("Excel 导出失败", e);
        }
    }

    /**
     * 导出为 JSON 格式
     */
    public byte[] exportToJson(List<TreeDTO> trees, Integer standId) {
        logger.info("开始导出 JSON 格式，林分ID: {}，记录数: {}", standId, trees.size());

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            ExportResult result = new ExportResult();
            result.setStandId(standId);
            result.setTotalCount(trees.size());
            result.setTrees(trees);
            result.setExportTime(java.time.LocalDateTime.now().toString());

            String json = mapper.writeValueAsString(result);
            logger.info("JSON 导出完成，大小: {} bytes", json.getBytes(StandardCharsets.UTF_8).length);
            return json.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.error("JSON 导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 导出失败", e);
        }
    }

    /**
     * 将 TreeDTO 转换为字符串数组（按数据库字段顺序）
     */
    private String[] convertToStringArray(TreeDTO tree) {
        return new String[]{
                String.valueOf(tree.getTreeId()),
                String.valueOf(tree.getPlotId()),
                String.valueOf(tree.getTreeNo()),
                nullToEmpty(tree.getSpecies()),
                formatDouble(tree.getDbhAvg()),
                formatDouble(tree.getTreeHeight()),
                formatDouble(tree.getDiameterHalfHeight()),
                formatDouble(tree.getQ2()),
                formatDouble(tree.getF1()),
                formatDouble(tree.getBasalArea()),
                formatDouble(tree.getVolume()),
                formatDouble(tree.getCrownWidth()),
                formatDouble(tree.getDbhDirection1()),
                formatDouble(tree.getDbhDirection2()),
                nullToEmpty(tree.getHealthStatus()),
                nullToEmpty(tree.getSpeciesCode()),
                nullToEmpty(tree.getSurveyDate()),
                nullToEmpty(tree.getTreeQuality()),
                String.valueOf(tree.getStandId())
        };
    }

    /**
     * 填充 Excel 行数据
     */
    private void fillExcelRow(Row row, TreeDTO tree, CellStyle dataStyle, CellStyle numberStyle) {
        int col = 0;

        // 整数列
        row.createCell(col++).setCellValue(tree.getTreeId());
        row.createCell(col++).setCellValue(tree.getPlotId());
        row.createCell(col++).setCellValue(tree.getTreeNo());

        // 字符串列
        createCell(row, col++, tree.getSpecies(), dataStyle);

        // 数值列
        createNumericCell(row, col++, tree.getDbhAvg(), numberStyle);
        createNumericCell(row, col++, tree.getTreeHeight(), numberStyle);
        createNumericCell(row, col++, tree.getDiameterHalfHeight(), numberStyle);
        createNumericCell(row, col++, tree.getQ2(), numberStyle);
        createNumericCell(row, col++, tree.getF1(), numberStyle);
        createNumericCell(row, col++, tree.getBasalArea(), numberStyle);
        createNumericCell(row, col++, tree.getVolume(), numberStyle);
        createNumericCell(row, col++, tree.getCrownWidth(), numberStyle);
        createNumericCell(row, col++, tree.getDbhDirection1(), numberStyle);
        createNumericCell(row, col++, tree.getDbhDirection2(), numberStyle);

        // 字符串列
        createCell(row, col++, tree.getHealthStatus(), dataStyle);
        createCell(row, col++, tree.getSpeciesCode(), dataStyle);
        createCell(row, col++, tree.getSurveyDate(), dataStyle);
        createCell(row, col++, tree.getTreeQuality(), dataStyle);

        // 整数列
        row.createCell(col).setCellValue(tree.getStandId());
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, Double value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("0.00"));
        return style;
    }

    private String nullToEmpty(String str) {
        return str != null ? str : "";
    }

    private String formatDouble(Double value) {
        return value != null ? String.format("%.2f", value) : "";
    }

    @Setter
    @Getter
    public static class ExportResult {
        private Integer standId;
        private Integer totalCount;
        private List<TreeDTO> trees;
        private String exportTime;
    }
}