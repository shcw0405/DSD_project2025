package com.example.patientmanagementsystem.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV数据处理工具类
 */
@Component // 添加@Component注解，使其被Spring容器管理
public class CSVProcessor {
    
    /**
     * 清洗CSV数据
     * 
     * @param rawData 原始CSV数据
     * @return 清洗后的CSV数据
     */
    public byte[] cleanData(byte[] rawData) { // 移除static关键字，使其成为实例方法
        if (rawData == null || rawData.length == 0) {
            throw new RuntimeException("CSV数据处理失败：输入数据为空");
        }
        
        try {
            // 读取CSV数据
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(rawData), StandardCharsets.UTF_8));
            
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim());
            
            List<CSVRecord> records = csvParser.getRecords();
            
            // 获取表头
            List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());
            
            // 检查CSV格式是否正确
            if (headers.size() < 7) {
                throw new RuntimeException("CSV数据处理失败：CSV格式不正确，列数不足");
            }
            
            // 创建输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), 
                    CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));
            
            // 处理每一行数据
            for (CSVRecord record : records) {
                List<String> rowData = new ArrayList<>();
                
                // 处理每一列数据
                for (String header : headers) {
                    String value = record.get(header);
                    
                    // 数据清洗逻辑
                    value = cleanValue(value, header);
                    
                    rowData.add(value);
                }
                
                // 写入清洗后的数据
                csvPrinter.printRecord(rowData);
            }
            
            csvPrinter.flush();
            csvPrinter.close();
            csvParser.close();
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("CSV数据处理失败", e);
        }
    }
    
    /**
     * 清洗单个值
     * 
     * @param value 原始值
     * @param header 列名
     * @return 清洗后的值
     */
    private String cleanValue(String value, String header) { // 移除static关键字，使其成为实例方法
        if (value == null || value.trim().isEmpty()) {
            return ""; // 空值处理
        }
        
        // 根据不同的列名进行不同的清洗
        switch (header.toLowerCase()) {
            case "time":
            case "timestamp":
                // 时间格式处理
                return cleanTimeValue(value);
                
            case "acc_x":
            case "acc_y":
            case "acc_z":
            case "gyr_x":
            case "gyr_y":
            case "gyr_z":
            case "mag_x":
            case "mag_y":
            case "mag_z":
                // 数值处理
                return cleanNumericValue(value);
                
            default:
                // 默认处理
                return value.trim();
        }
    }
    
    /**
     * 清洗时间值
     * 
     * @param value 原始时间值
     * @return 清洗后的时间值
     */
    private String cleanTimeValue(String value) { // 移除static关键字，使其成为实例方法
        // 移除非法字符
        value = value.replaceAll("[^0-9:.-]", "");
        return value;
    }
    
    /**
     * 清洗数值
     * 
     * @param value 原始数值
     * @return 清洗后的数值
     */
    private String cleanNumericValue(String value) { // 移除static关键字，使其成为实例方法
        try {
            // 尝试解析为数值
            double numValue = Double.parseDouble(value.trim());
            
            // 处理异常值
            if (Double.isNaN(numValue) || Double.isInfinite(numValue)) {
                return "0.0";
            }
            
            // 限制极端值
            if (numValue > 1000) {
                return "1000.0";
            } else if (numValue < -1000) {
                return "-1000.0";
            }
            
            return String.valueOf(numValue);
        } catch (NumberFormatException e) {
            // 无法解析为数值，返回0
            return "0.0";
        }
    }
}
