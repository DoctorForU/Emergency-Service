package com.example.EmergencyService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmergencyRequest { // 실시간 응급 상황판
    private String selectedCity; // 주소(시도)
    private String selectedDistrict; // 주소(시군구)
}
