package com.example.EmergencyService.controller;

import com.example.EmergencyService.dto.EmergencyMessageRequest;
import com.example.EmergencyService.dto.EmergencyRequest;
import com.example.EmergencyService.service.EmergencyMessage;
import com.example.EmergencyService.service.EmergencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emergency-service")
public class EmergencyController {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyController.class);

    @Autowired
    private EmergencyService emergencyService;
    @Autowired
    private EmergencyMessage emergencyMessage;

    @PostMapping("/emergencyList") // 응급 실시간 현황데이터
    public ResponseEntity<?> getEmergencyCall(@RequestBody EmergencyRequest emergencyRequest){
        logger.info("STAGE1: " + emergencyRequest.getSTAGE1());
        logger.info("STAGE2: " + emergencyRequest.getSTAGE2());

        return ResponseEntity.ok(emergencyService.callRealtimeEmergency(emergencyRequest));
    }

    @GetMapping("/emergencyMessage") // 실시간 응급 메시지 받기
    public ResponseEntity<?> getEmergencyMessage(@RequestParam String hpid){
        logger.info("Received hpid: " + hpid);

        return ResponseEntity.ok(emergencyMessage.getMessage(hpid));
    }



}
