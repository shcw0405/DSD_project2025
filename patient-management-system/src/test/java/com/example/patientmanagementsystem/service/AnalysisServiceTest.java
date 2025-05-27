package com.example.patientmanagementsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AnalysisServiceTest {

    @InjectMocks
    private AnalysisService analysisService;

    private byte[] sampleCleanedData;

    @BeforeEach
    void setUp() {
        // 创建一些样本数据
        sampleCleanedData = new byte[100]; // 例如100字节的数据
        for (int i = 0; i < sampleCleanedData.length; i++) {
            sampleCleanedData[i] = (byte) (i % 256);
        }
    }

    @Test
    void testStatistic_ReturnsExpectedStructureAndMetrics() {
        // 执行
        Map<String, Object> result = analysisService.statistic(sampleCleanedData);

        // 验证返回结果的基本结构
        assertNotNull(result, "结果不应为 null");
        assertTrue(result.containsKey("analysisTime"), "结果应包含 'analysisTime'");
        assertTrue(result.containsKey("dataPoints"), "结果应包含 'dataPoints'");
        assertTrue(result.containsKey("metrics"), "结果应包含 'metrics'");
        assertTrue(result.containsKey("conclusion"), "结果应包含 'conclusion'");

        // 验证 'analysisTime' 是一个 Long 类型 (时间戳)
        Object analysisTime = result.get("analysisTime");
        assertNotNull(analysisTime, "'analysisTime' 不应为 null");
        assertInstanceOf(Long.class, analysisTime, "'analysisTime' 应为 Long 类型");

        // 验证 'dataPoints' (根据模拟逻辑，这里是 rawData.length / 10)
        Object dataPoints = result.get("dataPoints");
        assertNotNull(dataPoints, "'dataPoints' 不应为 null");
        assertInstanceOf(Integer.class, dataPoints, "'dataPoints' 应为 Integer 类型");
        assertEquals(sampleCleanedData.length / 10, dataPoints, "'dataPoints' 的值不符合预期");

        // 验证 'conclusion' 是一个 String 类型
        Object conclusion = result.get("conclusion");
        assertNotNull(conclusion, "'conclusion' 不应为 null");
        assertInstanceOf(String.class, conclusion, "'conclusion' 应为 String 类型");
        assertFalse(((String) conclusion).isEmpty(), "'conclusion' 不应为空字符串");

        // 验证 'metrics' 是一个 Map
        Object metricsObject = result.get("metrics");
        assertNotNull(metricsObject, "'metrics' 不应为 null");
        assertInstanceOf(Map.class, metricsObject, "'metrics' 应为 Map 类型");

        @SuppressWarnings("unchecked")
        Map<String, Double> metrics = (Map<String, Double>) metricsObject;
        
        // 验证 'metrics' 中包含预期的模拟指标 (根据 AnalysisService 中的模拟逻辑)
        assertTrue(metrics.containsKey("averageAcceleration"), "Metrics 应包含 'averageAcceleration'");
        assertTrue(metrics.containsKey("maxAcceleration"), "Metrics 应包含 'maxAcceleration'");
        assertTrue(metrics.containsKey("minAcceleration"), "Metrics 应包含 'minAcceleration'");
        assertTrue(metrics.containsKey("standardDeviation"), "Metrics 应包含 'standardDeviation'");
        assertTrue(metrics.containsKey("gaitCadence"), "Metrics 应包含 'gaitCadence'");
        assertTrue(metrics.containsKey("stepLength"), "Metrics 应包含 'stepLength'");
        assertTrue(metrics.containsKey("walkingSpeed"), "Metrics 应包含 'walkingSpeed'");
        assertTrue(metrics.containsKey("symmetryIndex"), "Metrics 应包含 'symmetryIndex'");

        // 验证模拟指标的值（如果这些值是固定的，可以进行精确断言）
        assertEquals(9.8, metrics.get("averageAcceleration"), 0.001, "'averageAcceleration' 值不符合预期");
        assertEquals(12.3, metrics.get("maxAcceleration"), 0.001, "'maxAcceleration' 值不符合预期");
        // ... 可以为其他指标添加类似的值验证
    }

    @Test
    void testStatistic_WithEmptyData() {
        // 准备
        byte[] emptyData = new byte[0];

        // 执行
        Map<String, Object> result = analysisService.statistic(emptyData);

        // 验证 (根据当前 AnalysisService 的实现，即使是空数据，也会返回包含模拟指标的结构)
        assertNotNull(result, "即使输入为空，结果也不应为 null");
        assertTrue(result.containsKey("analysisTime"), "结果应包含 'analysisTime'");
        assertEquals(0, result.get("dataPoints"), "对于空数据，'dataPoints' 应为0"); 
        assertTrue(result.containsKey("metrics"), "结果应包含 'metrics'");
        assertTrue(result.containsKey("conclusion"), "结果应包含 'conclusion'");

        @SuppressWarnings("unchecked")
        Map<String, Double> metrics = (Map<String, Double>) result.get("metrics");
        assertNotNull(metrics, "'metrics' 不应为 null 即使输入为空");
        // 模拟指标仍然会被填充
        assertTrue(metrics.containsKey("averageAcceleration"), "即使输入为空，Metrics 也应包含 'averageAcceleration'");
    }

    @Test
    void testStatistic_WithNullData() {
        // 执行和验证：当前的实现，如果传入 null，会在 rawData.length 处抛出 NullPointerException
        // 如果期望服务能处理 null 输入（例如返回特定错误或默认值），则需要修改服务代码
        // 这里我们测试当前行为，即抛出 NullPointerException
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            analysisService.statistic(null);
        });
        // 可以根据需要添加对异常消息的验证，但通常验证类型就足够了
        assertNotNull(exception, "当输入为null时，应抛出NullPointerException");
    }
} 