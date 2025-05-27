package com.example.patientmanagementsystem.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CSVProcessorTest {

    private final CSVProcessor csvProcessor = new CSVProcessor(); // 实例化CSVProcessor

    @Test
    void testCleanData_Success() {
        // 准备测试数据
        String csvData = "time,acc_x,acc_y,acc_z,gyr_x,gyr_y,gyr_z\n" +
                "2025-04-18 08:39:33.123,1.234,2.345,3.456,0.123,0.234,0.345\n" +
                "2025-04-18 08:39:33.223,invalid,2.345,3.456,0.123,0.234,0.345\n" +
                "2025-04-18 08:39:33.323,1.234,2.345,3.456,NaN,0.234,0.345\n" +
                "2025-04-18 08:39:33.423,1.234,2.345,3.456,0.123,0.234,invalid\n";
        
        byte[] rawData = csvData.getBytes(StandardCharsets.UTF_8);
        
        // 执行 - 使用实例方法而非静态方法
        byte[] cleanedData = csvProcessor.cleanData(rawData);
        
        // 验证
        assertNotNull(cleanedData);
        assertTrue(cleanedData.length > 0);
        
        String cleanedCsv = new String(cleanedData, StandardCharsets.UTF_8);
        
        // 验证标题行保留
        assertTrue(cleanedCsv.contains("time,acc_x,acc_y,acc_z,gyr_x,gyr_y,gyr_z"));
        
        // 验证有效数据保留
        assertTrue(cleanedCsv.contains("2025-04-18 08:39:33.123,1.234,2.345,3.456,0.123,0.234,0.345"));
        
        // 验证无效数据被清洗
        assertFalse(cleanedCsv.contains("invalid"));
        assertFalse(cleanedCsv.contains("NaN"));
        
        // 验证无效数据被替换为0.0
        assertTrue(cleanedCsv.contains("0.0"));
    }
    
    @Test
    void testCleanData_EmptyInput() {
        // 准备空数据
        byte[] emptyData = "".getBytes(StandardCharsets.UTF_8);
        
        // 执行和验证 - 使用实例方法而非静态方法
        Exception exception = assertThrows(RuntimeException.class, () -> {
            csvProcessor.cleanData(emptyData);
        });
        
        assertTrue(exception.getMessage().contains("CSV数据处理失败"));
    }
    
    @Test
    void testCleanData_MalformedCSV() {
        // 准备格式错误的CSV数据
        String malformedCsv = "time,acc_x,acc_y\n" +
                "2025-04-18 08:39:33.123,1.234\n"; // 少一列
        
        byte[] malformedData = malformedCsv.getBytes(StandardCharsets.UTF_8);
        
        // 执行和验证 - 使用实例方法而非静态方法
        Exception exception = assertThrows(RuntimeException.class, () -> {
            csvProcessor.cleanData(malformedData);
        });
        
        assertTrue(exception.getMessage().contains("CSV数据处理失败"));
    }
}
