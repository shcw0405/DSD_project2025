package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.UploadResponseDataDTO;
import com.example.patientmanagementsystem.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 数据上传控制器
 * 处理CSV文件上传和处理
 */
@RestController
@RequestMapping("/upload")
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    private final DataService dataService;

    @Autowired
    public UploadController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 上传四个IMU数据CSV文件
     * @param file1 第一个CSV文件
     * @param file2 第二个CSV文件
     * @param file3 第三个CSV文件
     * @param file4 第四个CSV文件
     * @param patientId 患者ID
     * @return 上传和处理结果，包含报告ID和数据
     */
    @PostMapping("/csv")
    public ResponseEntity<ApiResponse<UploadResponseDataDTO>> uploadFourCsvFiles(
            @RequestPart("file1") MultipartFile file1,
            @RequestPart("file2") MultipartFile file2,
            @RequestPart("file3") MultipartFile file3,
            @RequestPart("file4") MultipartFile file4,
            @RequestParam("patientId") String patientId) {
        
        logger.info("收到CSV文件上传请求: patientId={}, file1={}, file2={}, file3={}, file4={}", 
                patientId, file1.getOriginalFilename(), file2.getOriginalFilename(), 
                file3.getOriginalFilename(), file4.getOriginalFilename());

        List<MultipartFile> files = Arrays.asList(file1, file2, file3, file4);

        // 基本的文件校验 (空文件, 非CSV)
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                logger.warn("上传失败: 文件 {} 为空", file.getOriginalFilename());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest("请求错误: 文件 '" + file.getOriginalFilename() + "' 为空"));
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
                logger.warn("上传失败: 文件 {} 不是CSV格式", originalFilename);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest("请求错误: 文件 '" + originalFilename + "' 类型必须为 CSV"));
            }
        }
        
        // 调用服务层处理
        // 注意：dataService.processFourCsvFiles 方法尚不存在，需要创建
        UploadResponseDataDTO responseData = dataService.processAndAnalyzeFourCsvFiles(files, patientId);
        
        // 成功时返回200 OK，因为API文档示例是200，不是201
        return ResponseEntity.ok(ApiResponse.success(responseData, "CSV 文件上传成功"));
    }
    
    /**
     * 获取数据处理记录
     * @param patientId 患者ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 数据处理记录列表和总数
     */
    @GetMapping("/records/{patientId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDataRecords(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        logger.info("请求获取患者 {} 的数据处理记录, page={}, pageSize={}", patientId, page, pageSize);
        Map<String, Object> result = dataService.getPatientDataRecords(patientId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result, "获取数据处理记录成功"));
    }
}
