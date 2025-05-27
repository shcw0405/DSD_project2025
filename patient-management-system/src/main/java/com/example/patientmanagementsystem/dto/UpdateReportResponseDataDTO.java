package com.example.patientmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportResponseDataDTO {
    private String reportId;
    private UpdatedFieldsDTO updatedFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatedFieldsDTO {
        private String type;
        private String summary;
        @JsonProperty("updatedAt") // To match the API spec string format
        private String updatedAt; // ISO String format
    }
} 