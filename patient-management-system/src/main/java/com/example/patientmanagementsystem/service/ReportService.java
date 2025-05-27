package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.PatientReportDTO;
import com.example.patientmanagementsystem.dto.PatientReportDetailDTO;
import com.example.patientmanagementsystem.dto.ReportDataDetailsDTO;
import com.example.patientmanagementsystem.dto.ReportDownloadDTO;
import com.example.patientmanagementsystem.dto.UpdateReportRequestDTO;
import com.example.patientmanagementsystem.dto.UpdateReportResponseDataDTO;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.PatientReport;
import com.example.patientmanagementsystem.model.Record;
import com.example.patientmanagementsystem.repository.PatientReportRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.RecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 报告服务
 * 处理患者报告相关的业务逻辑
 */
@Service
public class ReportService {

    private final PatientReportRepository reportRepository;
    private final PatientRepository patientRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    public ReportService(PatientReportRepository reportRepository, PatientRepository patientRepository,
                        RecordRepository recordRepository, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.patientRepository = patientRepository;
        this.recordRepository = recordRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取患者报告列表 (旧版，可能用于管理端分页)
     * @param patientId 患者ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 报告列表和总数
     */
    public Map<String, Object> getPatientReports(String patientId, int page, int pageSize) {
        if (!patientRepository.existsById(patientId)) {
            throw new EntityNotFoundException("未找到该患者");
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "date")); // Sort by date
        
        // This repository method already returns Page<PatientReport> if defined correctly,
        // otherwise, we need to fetch List and manually paginate.
        // Assuming findByPatient_IdOrderByDateDesc returns List for now as per previous context.
        List<PatientReport> allReportsForPatient = reportRepository.findByPatient_IdOrderByDateDesc(patientId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allReportsForPatient.size());
        List<PatientReport> pageContent = new ArrayList<>();
        if (start <= end && start < allReportsForPatient.size()) { // ensure start is within bounds
            pageContent = allReportsForPatient.subList(start, Math.min(end, allReportsForPatient.size()));
        }
        
        Page<PatientReport> reportsPage = new PageImpl<>(pageContent, pageable, allReportsForPatient.size());
        
        List<Map<String, Object>> reportsList = reportsPage.getContent().stream()
                .map(report -> {
                    Map<String, Object> reportMap = new HashMap<>();
                    reportMap.put("id", report.getId());
                    reportMap.put("patientId", report.getPatient().getId());
                    reportMap.put("recordId", report.getRecordId());
                    reportMap.put("type", report.getType()); // Use 'type'
                    reportMap.put("summary", report.getSummary());
                    reportMap.put("date", report.getDate() != null ? report.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
                    reportMap.put("createdAt", report.getCreatedAt() != null ? report.getCreatedAt().toString() : null);
                    reportMap.put("updatedAt", report.getUpdatedAt() != null ? report.getUpdatedAt().toString() : null);
                    return reportMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", reportsList);
        result.put("total", reportsPage.getTotalElements());
        
        return result;
    }

    /**
     * 获取报告详情
     * @param reportId 报告ID
     * @return 报告详情 (PatientReportDetailDTO)
     */
    public PatientReportDetailDTO getReportDetail(String reportId) {
        PatientReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("未找到该报告, ID: " + reportId));
        
        // Ensure patient is not null before trying to get its ID
        if (report.getPatient() == null) {
            throw new BusinessException("报告 " + reportId + " 没有关联的患者信息。", 500);
        }
        Patient patient = patientRepository.findById(report.getPatient().getId())
                .orElseThrow(() -> new EntityNotFoundException("未找到关联患者, ID: " + report.getPatient().getId()));
        
        Record record = null;
        if (report.getRecordId() != null && !report.getRecordId().isEmpty()) {
             record = recordRepository.findById(report.getRecordId())
                .orElse(null); // Changed to orElse(null) to not fail if record is optional
        }
        
        PatientReportDetailDTO reportDTO = new PatientReportDetailDTO();
        reportDTO.setId(report.getId());
        reportDTO.setPatientId(report.getPatient().getId());
        reportDTO.setPatientName(patient.getName()); // Patient name comes from Patient entity
        reportDTO.setRecordId(report.getRecordId());
        if (record != null) {
            reportDTO.setFileName(record.getFileName());
        } else {
            reportDTO.setFileName(null); // Explicitly set to null if no record
        }
        reportDTO.setType(report.getType());
        reportDTO.setSummary(report.getSummary());
        reportDTO.setCreatedAt(report.getCreatedAt());
        reportDTO.setUpdatedAt(report.getUpdatedAt());
        reportDTO.setDate(report.getDate() != null ? report.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);

        if (report.getReportDataJson() != null && !report.getReportDataJson().isEmpty()) {
            try {
                ReportDataDetailsDTO details = objectMapper.readValue(report.getReportDataJson(), ReportDataDetailsDTO.class);
                reportDTO.setReportDataDetails(details);
            } catch (JsonProcessingException e) {
                throw new BusinessException("报告数据解析失败: " + report.getId(), 500, e);
            }
        } else {
             reportDTO.setReportDataDetails(new ReportDataDetailsDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }
        
        return reportDTO;
    }

    /**
     * 更新报告类型和概要信息
     * @param reportId 报告ID
     * @param type 新的报告类型
     * @param summary 新的报告概要
     * @return UpdateReportResponseDataDTO 包含更新后的信息
     */
    @Transactional
    public UpdateReportResponseDataDTO updateReportDetails(String reportId, String type, String summary) {
        PatientReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("报告不存在，ID: " + reportId));

        report.setType(type);
        report.setSummary(summary);
        
        PatientReport savedReport = reportRepository.save(report);

        UpdateReportResponseDataDTO.UpdatedFieldsDTO updatedFields = new UpdateReportResponseDataDTO.UpdatedFieldsDTO(
                savedReport.getType(),
                savedReport.getSummary(),
                savedReport.getUpdatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );

        return new UpdateReportResponseDataDTO(savedReport.getId(), updatedFields);
    }

    /**
     * 创建报告
     * @param patientId 患者ID
     * @param recordId 记录ID (可选)
     * @param type 报告类型
     * @param summary 报告摘要
     * @param reportDataMap 报告数据 (将被序列化为JSON)
     * @return 创建的报告详情 (PatientReportDetailDTO)
     */
    @Transactional
    public PatientReportDetailDTO createReport(String patientId, String recordId, String type, String summary, Map<String, Object> reportDataMap) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("创建报告失败：未找到患者, ID: " + patientId, 404));
        
        Record recordEntity = null;
        if (recordId != null && !recordId.isEmpty()) {
            // Allow record to be optional, don't throw if not found, just proceed without it
            recordEntity = recordRepository.findById(recordId).orElse(null);
        }
        
        PatientReport report = new PatientReport();
        report.setPatient(patient);
        report.setRecordId(recordId); // Can be null if recordEntity is null or recordId was initially null/empty
        report.setDate(LocalDate.now()); // Default report date to now
        report.setType(type);
        report.setSummary(summary);
        if (reportDataMap != null) {
            report.setReportData(reportDataMap); // This map will be serialized to reportDataJson by @PrePersist
        }
        
        PatientReport savedReport = reportRepository.save(report); // createdAt and updatedAt are set by @Timestamps
        
        // Convert saved entity to DTO
        PatientReportDetailDTO reportDTO = new PatientReportDetailDTO();
        reportDTO.setId(savedReport.getId());
        reportDTO.setPatientId(savedReport.getPatient().getId());
        reportDTO.setPatientName(patient.getName());
        reportDTO.setRecordId(savedReport.getRecordId());
        if (recordEntity != null) {
            reportDTO.setFileName(recordEntity.getFileName());
        } else {
            reportDTO.setFileName(null);
        }
        reportDTO.setType(savedReport.getType());
        reportDTO.setSummary(savedReport.getSummary());
        reportDTO.setCreatedAt(savedReport.getCreatedAt());
        reportDTO.setUpdatedAt(savedReport.getUpdatedAt());
        reportDTO.setDate(savedReport.getDate() != null ? savedReport.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);

        // Parse reportDataJson from the saved entity (which should have been populated by @PrePersist)
        if (savedReport.getReportDataJson() != null && !savedReport.getReportDataJson().isEmpty()) {
            try {
                ReportDataDetailsDTO details = objectMapper.readValue(savedReport.getReportDataJson(), ReportDataDetailsDTO.class);
                reportDTO.setReportDataDetails(details);
            } catch (JsonProcessingException e) {
                throw new BusinessException("新创建的报告数据解析失败: " + savedReport.getId(), 500, e);
            }
        } else {
             reportDTO.setReportDataDetails(new ReportDataDetailsDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }
        
        return reportDTO;
    }
    
    /**
     * 保存分析数据和相关的CSV文件（可选）为报告。
     * @param patientId 患者ID
     * @param type 报告类型
     * @param summary 报告摘要
     * @param reportDataMap 分析得出的数据
     * @param rawCsvContents Map, key like "rawCsvContent1", value is the string content of the CSV. Max 4.
     * @return 创建的报告详情
     */
    @Transactional
    public PatientReportDetailDTO saveAnalyzedReportWithCsvData(
            String patientId, 
            String type, 
            String summary, 
            Map<String, Object> reportDataMap,
            Map<String, String> rawCsvContents) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("创建报告失败：未找到患者, ID: " + patientId, 404));
        
        PatientReport report = new PatientReport();
        report.setPatient(patient);
        report.setDate(LocalDate.now());
        report.setType(type);
        report.setSummary(summary);
        if (reportDataMap != null) {
            report.setReportData(reportDataMap);
        }

        if (rawCsvContents != null) {
            report.setRawCsvContent1(rawCsvContents.get("rawCsvContent1"));
            report.setRawCsvContent2(rawCsvContents.get("rawCsvContent2"));
            report.setRawCsvContent3(rawCsvContents.get("rawCsvContent3"));
            report.setRawCsvContent4(rawCsvContents.get("rawCsvContent4"));
        }
        
        PatientReport savedReport = reportRepository.save(report);
        
        PatientReportDetailDTO reportDTO = new PatientReportDetailDTO();
        reportDTO.setId(savedReport.getId());
        reportDTO.setPatientId(savedReport.getPatient().getId());
        reportDTO.setPatientName(patient.getName());
        reportDTO.setType(savedReport.getType());
        reportDTO.setSummary(savedReport.getSummary());
        reportDTO.setCreatedAt(savedReport.getCreatedAt());
        reportDTO.setUpdatedAt(savedReport.getUpdatedAt());
        reportDTO.setDate(savedReport.getDate() != null ? savedReport.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
        // recordId and fileName will be null as we don't create/link a Record entity here

        if (savedReport.getReportDataJson() != null && !savedReport.getReportDataJson().isEmpty()) {
            try {
                ReportDataDetailsDTO details = objectMapper.readValue(savedReport.getReportDataJson(), ReportDataDetailsDTO.class);
                reportDTO.setReportDataDetails(details);
            } catch (JsonProcessingException e) {
                throw new BusinessException("新创建的报告(含CSV)数据解析失败: " + savedReport.getId(), 500, e);
            }
        } else {
             reportDTO.setReportDataDetails(new ReportDataDetailsDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }
        return reportDTO;
    }
    
    public ReportDownloadDTO getReportDownloadInfo(String reportId) {
        PatientReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("未找到该报告, ID: " + reportId));
        
        if (report.getRecordId() == null || report.getRecordId().isEmpty()){
            throw new EntityNotFoundException("该报告没有关联的可下载文件记录。");
        }

        Record record = recordRepository.findById(report.getRecordId())
                .orElseThrow(() -> new EntityNotFoundException("未找到关联记录, ID: " + report.getRecordId()));
        
        ReportDownloadDTO downloadInfo = new ReportDownloadDTO();
        downloadInfo.setFileName(record.getFileName());
        downloadInfo.setFilePath(record.getFilePath());
        
        return downloadInfo;
    }

    public List<PatientReportDTO> getReportsForPatient(String patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new EntityNotFoundException("未找到指定的患者, ID: " + patientId);
        }
        List<PatientReport> reports = reportRepository.findByPatient_IdOrderByDateDesc(patientId);
        if (reports.isEmpty()) {
            return new ArrayList<>();
        }
        return reports.stream().map(report -> {
            PatientReportDTO dto = new PatientReportDTO();
            dto.setDate(report.getDate() != null ? report.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
            dto.setType(report.getType());
            dto.setSummary(report.getSummary());
            if (report.getReportDataJson() != null && !report.getReportDataJson().isEmpty()) {
                try {
                    ReportDataDetailsDTO details = objectMapper.readValue(report.getReportDataJson(), ReportDataDetailsDTO.class);
                    dto.setReportDataDetails(details);
                } catch (JsonProcessingException e) {
                    throw new BusinessException("报告数据解析失败: " + report.getId(), 500, e);
                }
            } else {
                 dto.setReportDataDetails(new ReportDataDetailsDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 上传单个报告文件，保存文件，创建Record实体，并创建PatientReport条目.
     * @param file MultipartFile 报告文件
     * @param patientId 患者ID
     * @param type 报告类型
     * @param summary 报告摘要
     * @return 创建的报告详情 (PatientReportDetailDTO)
     * @throws BusinessException 如果文件上传失败或患者不存在
     */
    @Transactional
    public PatientReportDetailDTO uploadReportFileAndCreateRecord(MultipartFile file, String patientId, String type, String summary) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("上传报告失败：未找到患者, ID: " + patientId, 404));

        if (file.isEmpty()) {
            throw new BusinessException("上传报告失败：文件为空", 400);
        }

        String recordId;
        String originalFilename = file.getOriginalFilename();
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            Files.copy(file.getInputStream(), filePath);
            
            Record record = new Record();
            record.setPatientId(patientId); // 使用 setPatientId
            record.setFileName(originalFilename);
            record.setFilePath(filePath.toString());
            record.setUploadTime(LocalDateTime.now());
            
            Record savedRecord = recordRepository.save(record);
            recordId = savedRecord.getId();

        } catch (IOException e) {
            throw new BusinessException("文件上传处理失败: " + e.getMessage(), 500, e);
        }

        // 调用现有的 createReport, reportDataMap 为 null 因为内容在文件中
        return createReport(patientId, recordId, type, summary, null);
    }
}
