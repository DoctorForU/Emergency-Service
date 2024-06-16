package com.example.EmergencyService.dto;

import lombok.Data;

@Data
public class EmergencyMessageResponse {
    private String hpid;
    private String symBlkMsg; // 전달메시지
    private String symBlkMsgTyp; // 메시지 구분 A:응급, B:중증
    private String symBlkSttDtm; // 메시지 시작일시 2016 12 01 13 00 00
    private String symBlkEndDtm; // 메시지 해제일시 2016 12 01 13 00 00

}
