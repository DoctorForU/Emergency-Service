package com.example.EmergencyService.dto;

import lombok.Data;

@Data
public class EmergencyListResponse {
    private String HPID;
    private String dutyAddr; // 주소
    private String dutyName; // 병원명
    private String dutyTel1; // 대표전화
}
