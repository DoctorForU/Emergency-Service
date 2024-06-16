package com.example.EmergencyService.dto;


import lombok.Data;

@Data
public class EmergencyListRequest {
    private String Q0; // 주소(시/도)
    private String Q1; // 주소(시/군/구)
    private String QT; // 진료요일( 1~7 ) 공휴일 8
    private String QD; // 진료과목
}
