package com.example.EmergencyService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmergencyService { // 응급실 실시간 가용병상정보 조회 -> 응급실 데이터
    //요청 STAGE1 : 시도
    // STAGE2: 시군구
    private static final Logger logger = LoggerFactory.getLogger(EmergencyService.class);

    private static final String PUBLIC_DATA_API_URL = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire";
    private static final String SERVICE_KEY = "K9t4%2FMS1InyhHxC7oJtTEGncK1mWLav7ML0G5XcgX7k37YyN6sL7owPZDulwsO7m0jyVwvEqeoiFQp3c7C%2BKuQ%3D%3D";
    private static final String NUM_OF_ROWS = "3000";
    private static final String PAGE_NO = "1";


}
