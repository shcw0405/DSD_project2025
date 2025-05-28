package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.analysis.Analysis;
import com.example.patientmanagementsystem.dto.ReportDataDTO;
import com.example.patientmanagementsystem.dto.UploadResponseDataDTO;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.exception.CsvValidationException;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.PatientReport;
import com.example.patientmanagementsystem.model.Record;
import com.example.patientmanagementsystem.repository.PatientReportRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.RecordRepository;
import com.example.patientmanagementsystem.util.CSVProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据服务
 * 处理CSV文件上传和处理
 */
@Service
public class DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    private final RecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final PatientReportRepository patientReportRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.python.command:python}")
    private String pythonCommand;

    private String pythonScriptPath;

    @Value("${app.python.script.frequency:10}")
    private int pythonScriptFrequency;
    
    private static final Pattern FILENAME_PATTERN = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-[1-4]\\.csv$"
    );

    @Autowired
    public DataService(RecordRepository recordRepository,
                       PatientRepository patientRepository,
                       PatientReportRepository patientReportRepository) {
        this.recordRepository = recordRepository;
        this.patientRepository = patientRepository;
        this.patientReportRepository = patientReportRepository;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource scriptResource = new ClassPathResource("scripts/processorOfRawData.py");
            if (!scriptResource.exists()) {
                logger.error("Python脚本在类路径中未找到: scripts/processorOfRawData.py");
                throw new RuntimeException("关键Python脚本在类路径 scripts/processorOfRawData.py 中未找到。应用无法正确处理CSV上传。");
            }
            File tempScriptDir = new File(System.getProperty("java.io.tmpdir"), "pms_scripts_" + UUID.randomUUID().toString().substring(0, 8));
            if (!tempScriptDir.exists()) {
                tempScriptDir.mkdirs();
            }
            tempScriptDir.deleteOnExit();

            File tempScriptFile = new File(tempScriptDir, "processorOfRawData.py");
            FileUtils.copyInputStreamToFile(scriptResource.getInputStream(), tempScriptFile);
            if (!tempScriptFile.setExecutable(true)) {
                logger.warn("无法将临时Python脚本设置为可执行: {}", tempScriptFile.getAbsolutePath());
            }
            tempScriptFile.deleteOnExit();
            
            this.pythonScriptPath = tempScriptFile.getAbsolutePath();
            logger.info("Python脚本已复制到临时可执行路径: {}", this.pythonScriptPath);

        } catch (IOException e) {
            logger.error("无法将Python脚本复制到临时位置", e);
            throw new RuntimeException("无法初始化Python脚本，CSV上传功能将无法使用。", e);
        }
    }

    @Transactional
    public UploadResponseDataDTO processAndAnalyzeFourCsvFiles(List<MultipartFile> files, String patientIdStr) {
        if (this.pythonScriptPath == null || this.pythonScriptPath.isEmpty()) {
            logger.error("Python脚本路径未初始化。CSV处理无法继续。");
            throw new BusinessException("服务器配置错误：Python脚本路径未就绪。");
        }
        Patient patient = patientRepository.findByUser_Id(patientIdStr)
                .orElseThrow(() -> {
                    logger.warn("上传CSV失败：未找到与用户ID '{}' 关联的患者记录.", patientIdStr);
                    return new BusinessException("请求错误：未找到与用户ID '" + patientIdStr + "' 关联的患者记录");
                });

        List<File> tempRawFiles = new ArrayList<>();
        List<String> rawFileContentsForDb = new ArrayList<>();
        List<File> tempCleanedFiles = new ArrayList<>();
        List<String> cleanedFileContentsForDb = new ArrayList<>();
        List<String> originalFilenames = new ArrayList<>();
        Path tempUploadDir = null;
        File pythonScriptWorkingDir = null;

        try {
            tempUploadDir = Paths.get(uploadDir, "temp", UUID.randomUUID().toString());
            Files.createDirectories(tempUploadDir);
            logger.info("创建临时上传目录: {}", tempUploadDir.toString());

            if (pythonScriptPath != null) {
                 pythonScriptWorkingDir = new File(pythonScriptPath).getParentFile();
            }
            if (pythonScriptWorkingDir == null) {
                logger.error("无法确定Python脚本的工作目录。");
                throw new BusinessException("服务器配置错误：无法确定Python脚本工作目录。");
            }

            for (MultipartFile multipartFile : files) {
                String originalFilename = multipartFile.getOriginalFilename();
                originalFilenames.add(originalFilename);

                if (originalFilename == null || !FILENAME_PATTERN.matcher(originalFilename).matches()) {
                    logger.warn("文件名 {} 不符合格式 YYYY-MM-DD-HH-mm-ss-N.csv", originalFilename);
                    throw new BusinessException("请求错误：文件名 '" + originalFilename + "' 不符合 'YYYY-MM-DD-HH-mm-ss-N.csv' 格式");
                }

                File tempRawFile = tempUploadDir.resolve(originalFilename).toFile();
                
                String rawContent = new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
                rawFileContentsForDb.add(rawContent);
                
                FileUtils.writeByteArrayToFile(tempRawFile, multipartFile.getBytes());

                tempRawFiles.add(tempRawFile);
                logger.info("临时原始文件已保存: {}, 内容已读取.", tempRawFile.getAbsolutePath());

                ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    this.pythonScriptPath,
                    tempRawFile.getAbsolutePath(),
                    String.valueOf(pythonScriptFrequency)
                );
                processBuilder.redirectErrorStream(true);
                
                logger.info("执行Python脚本: {}", String.join(" ", processBuilder.command()));
                Process process = processBuilder.start();

                StringBuilder scriptOutput = new StringBuilder();
                String identifiedCleanedPath = null;
                try (BufferedReader scriptReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = scriptReader.readLine()) != null) {
                        scriptOutput.append(line).append(System.lineSeparator());
                        logger.debug("Python out: {}", line);
                        if (line.startsWith("CLEANED_FILE:")) {
                            identifiedCleanedPath = line.substring("CLEANED_FILE:".length()).trim();
                        }
                    }
                }

                boolean exited = process.waitFor(60, TimeUnit.SECONDS); 
                if (!exited) {
                    process.destroyForcibly(); 
                    logger.error("Python脚本执行超时 (60秒). 输出: {}", scriptOutput.toString());
                    throw new BusinessException("Python脚本执行超时。请检查脚本效率或数据量。");
                }
                
                int exitCode = process.exitValue(); 
                if (exitCode != 0) {
                    logger.error("Python脚本执行失败，退出码: {}. Python输出: {}", exitCode, scriptOutput.toString());
                    throw new BusinessException("Python脚本执行失败，退出码: " + exitCode + "。详情请查看日志。输出: " + scriptOutput.toString());
                }

                if (identifiedCleanedPath != null) {
                    File cleanedFile = new File(identifiedCleanedPath);
                    if (cleanedFile.exists()) {
                        tempCleanedFiles.add(cleanedFile);
                        logger.info("Python脚本生成的Cleaned文件: {}", cleanedFile.getAbsolutePath());
                    } else {
                        logger.error("Python脚本声称生成了文件，但未找到: {}. Python输出: {}", identifiedCleanedPath, scriptOutput.toString());
                        throw new BusinessException("Python脚本处理失败：未找到生成的cleaned文件。");
                    }
                } else {
                     logger.error("Python脚本未输出CLEANED_FILE路径. Python输出: {}", scriptOutput.toString());
                    throw new BusinessException("Python脚本处理失败：未获取到处理后的文件路径。");
                }
            }

            if (tempCleanedFiles.size() != 4) {
                 logger.error("Python脚本未能为所有原始文件生成cleaned文件. Expected 4, Got: {}.", tempCleanedFiles.size());
                throw new BusinessException("Python脚本处理失败：未能为所有4个文件生成预处理版本。");
            }
             if (rawFileContentsForDb.size() != 4) {
                logger.error("未能读取所有4个原始文件的内容进行数据库存储.");
                throw new BusinessException("服务器内部错误：读取原始文件内容失败。");
            }

            for (File cleanedFile : tempCleanedFiles) {
                cleanedFileContentsForDb.add(FileUtils.readFileToString(cleanedFile, StandardCharsets.UTF_8));
            }
            logger.info("所有4个cleaned文件已读取到字符串。 ({})", cleanedFileContentsForDb.size());

            Map<String, Double> analysisResultMap;
            try {
                analysisResultMap = Analysis.Statistic(
                    cleanedFileContentsForDb.get(0),
                    cleanedFileContentsForDb.get(1),
                    cleanedFileContentsForDb.get(2),
                    cleanedFileContentsForDb.get(3)
                );
            } catch (CsvValidationException e) {
                logger.warn("CSV 内容校验失败 for patientId: {}: {}", patientIdStr, e.getMessage());
                throw new BusinessException("请求错误: " + e.getMessage(), 400); 
            }
            logger.info("IMU数据分析完成. 结果: {}", analysisResultMap);

            ReportDataDTO reportMetrics = new ReportDataDTO();
            List<Double> standardAmplitudes = Arrays.asList(125.0, 125.0, 20.0, 20.0, 45.0, 45.0, 30.0, 30.0, 30.0, 30.0, 10.0, 10.0);
            List<Double> motionAmplitudes = new ArrayList<>();
            List<Double> differences = new ArrayList<>();
            String[] motionKeys = {
                "左前屈", "右前屈", "左后伸", "右后伸", "左外展", "右外展",
                "左内收", "右内收", "左外旋", "右外旋", "左内旋", "右内旋"
            };
            String[] diffKeys = {
                "左前屈差值", "右前屈差值", "左后伸差值", "右后伸差值", "左外展差值", "右外展差值",
                "左内收差值", "右内收差值", "左外旋差值", "右外旋差值", "左内旋差值", "右内旋差值"
            };
            for (String key : motionKeys) {
                motionAmplitudes.add(analysisResultMap.getOrDefault(key, 0.0));
            }
            for (String key : diffKeys) {
                differences.add(analysisResultMap.getOrDefault(key, 0.0));
            }
            reportMetrics.setStandardAmplitude(standardAmplitudes);
            reportMetrics.setMotionAmplitude(motionAmplitudes);
            reportMetrics.setDifference(differences);

            PatientReport report = new PatientReport();
            report.setPatient(patient);
            report.setDate(LocalDate.now());
            report.setType("IMU数据分析报告");
            report.setSummary("通过CSV上传自动生成的IMU分析报告。原始文件: " + String.join(", ", originalFilenames));
            
            Map<String, Object> reportDataMap = objectMapper.convertValue(reportMetrics, Map.class);
            report.setReportData(reportDataMap);

            if (rawFileContentsForDb.size() == 4) {
                report.setRawCsvContent1(rawFileContentsForDb.get(0));
                report.setRawCsvContent2(rawFileContentsForDb.get(1));
                report.setRawCsvContent3(rawFileContentsForDb.get(2));
                report.setRawCsvContent4(rawFileContentsForDb.get(3));
            }
            if (cleanedFileContentsForDb.size() == 4) {
                report.setCleanedCsvContent1(cleanedFileContentsForDb.get(0));
                report.setCleanedCsvContent2(cleanedFileContentsForDb.get(1));
                report.setCleanedCsvContent3(cleanedFileContentsForDb.get(2));
                report.setCleanedCsvContent4(cleanedFileContentsForDb.get(3));
            }

            PatientReport savedReport = patientReportRepository.save(report);
            logger.info("报告已保存，ID: {}", savedReport.getId());

            UploadResponseDataDTO responseData = new UploadResponseDataDTO();
            responseData.setReceivedAt(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
            responseData.setReportId(savedReport.getId());
            responseData.setReportData(reportMetrics);

            return responseData;

        } catch (BusinessException be) {
            logger.warn("业务异常处理CSV上传: {}. PatientId: {}", be.getMessage(), patientIdStr);
            throw be;
        } catch (IOException e) {
            logger.error("文件IO操作失败 during CSV processing for patientId: {}", patientIdStr, e);
            throw new BusinessException("服务器处理文件时发生IO错误：" + e.getMessage(), 500);
        } catch (InterruptedException e) {
            logger.error("Python脚本执行被中断 for patientId: {}", patientIdStr, e);
            Thread.currentThread().interrupt();
            throw new BusinessException("服务器处理超时或被中断，请稍后重试。", 500);
        } catch (Exception e) {
            logger.error("处理CSV文件时发生意外错误 for patientId: {}: {}", patientIdStr, e.getMessage(), e);
            if (e.getCause() instanceof CsvValidationException) {
                 throw new BusinessException("请求错误: CSV内容校验时发生意外问题 - " + e.getCause().getMessage(), 400);
            }
            throw new BusinessException("服务器内部错误，处理数据分析时出错: " + e.getMessage(), 500);
        } finally {
            logger.info("开始清理临时文件...");
            for (File tempFile : tempRawFiles) {
                FileUtils.deleteQuietly(tempFile); 
                logger.debug("已删除临时原始文件: {}", tempFile.getAbsolutePath());
            }
            for (File tempFile : tempCleanedFiles) {
                 FileUtils.deleteQuietly(tempFile);
                 logger.debug("已删除临时Cleaned文件: {}", tempFile.getAbsolutePath());
            }
            if (tempUploadDir != null && Files.exists(tempUploadDir)) {
                 try { 
                     FileUtils.deleteDirectory(tempUploadDir.toFile()); 
                     logger.info("已删除上传文件的临时目录: {}", tempUploadDir.toString());
                 } catch (IOException e) {
                     logger.warn("删除上传文件的临时目录 {} 失败", tempUploadDir.toString(), e);
                 }
            }
             logger.info("临时文件清理完成。");
        }
    }
    
    /**
     * 获取患者数据处理记录
     * @param patientId 患者ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 数据处理记录列表和总数
     */
    public Map<String, Object> getPatientDataRecords(String patientId, int page, int pageSize) {
        if (!patientRepository.existsById(patientId)) {
             logger.warn("请求数据记录失败: 患者ID '{}' 不存在.", patientId);
            throw new EntityNotFoundException("请求错误：患者 ID '" + patientId + "' 不存在");
        }
        
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "uploadTime"));
        Page<Record> recordsPage = recordRepository.findByPatientIdAndProcessedTrue(patientId, pageable);
        
        List<Map<String, Object>> recordsList = recordsPage.getContent().stream()
                .map(record -> {
                    Map<String, Object> recordMap = new HashMap<>();
                    recordMap.put("id", record.getId());
                    recordMap.put("fileName", record.getFileName());
                    recordMap.put("uploadTime", record.getUploadTime().format(DateTimeFormatter.ISO_DATE_TIME)); 
                    recordMap.put("processed", record.isProcessed());
                    recordMap.put("processedDataSummary", record.getProcessedData()); 
                    return recordMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", recordsList);
        result.put("total", recordsPage.getTotalElements());
        
        return result;
    }
}
