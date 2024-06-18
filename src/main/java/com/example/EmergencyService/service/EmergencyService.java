package com.example.EmergencyService.service;

import com.example.EmergencyService.dto.EmergencyRequest;
import com.example.EmergencyService.dto.EmergencyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmergencyService { // 응급실 실시간 가용병상정보 조회 -> 응급실 데이터
    //요청 STAGE1 : 시도
    // STAGE2: 시군구
    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.service.key}")
    private String SERVICE_KEY;

    private static final Logger logger = LoggerFactory.getLogger(EmergencyService.class);

    private static final String PUBLIC_DATA_API_URL = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire";
    //private static final String SERVICE_KEY = "K9t4%2FMS1InyhHxC7oJtTEGncK1mWLav7ML0G5XcgX7k37YyN6sL7owPZDulwsO7m0jyVwvEqeoiFQp3c7C%2BKuQ%3D%3D";
    private static final String NUM_OF_ROWS = "3000";
    private static final String PAGE_NO = "1";


    public List<EmergencyResponse> callRealtimeEmergency(EmergencyRequest request) { // 실시간 응급 상황 종합판
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(); // building 하는 요소들 제어
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); // 인코딩 자체를 멈추기 막아버리기
        restTemplate.setUriTemplateHandler(uriBuilderFactory); // 레스트 템플릿 빌딩 잡기

        String apiUrl = buildApiUrl(request);
        logger.info("Constructed API URL: " + apiUrl);
        String response = restTemplate.getForObject(apiUrl.replace("%25","%"), String.class); // 혹시 몰라서 한번 더 25 제거
        String utf8EncodedResponse = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8); // 한글 깨지는거 잡기

        logger.info("Response: " + utf8EncodedResponse);

        return parseXmlResponse(utf8EncodedResponse);
    }

    private String buildApiUrl(EmergencyRequest request) { // api 호출 url 만들기

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(PUBLIC_DATA_API_URL)
                .queryParam("pageNo", PAGE_NO)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("serviceKey", SERVICE_KEY)
                .encode()
                .build();

        StringBuilder apiUrl = new StringBuilder(builder.toString());


        if (request.getSelectedCity() != null && !request.getSelectedCity().isEmpty()) {
            apiUrl.append("&STAGE1=").append(encodeValue(request.getSelectedCity()));
        }
        if (request.getSelectedDistrict() != null && !request.getSelectedDistrict().isEmpty()) {
            apiUrl.append("&STAGE2=").append(encodeValue(request.getSelectedDistrict()));
        }
        String apiUrlStr = apiUrl.toString();
        logger.info("Final API URL: " + apiUrlStr);
        return apiUrlStr;
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<EmergencyResponse> parseXmlResponse(String response) {
        List<EmergencyResponse> emergencyResponses = new ArrayList<>();
        try {
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(response.getBytes(StandardCharsets.UTF_8));
            JsonNode items = root.path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    emergencyResponses.add(parseEmergencyResponse(item));
                }
            } else {
                emergencyResponses.add(parseEmergencyResponse(items));
            }
        } catch (Exception e) {
            logger.error("Error parsing XML response", e);
        }
        logger.info("Parsed emergencyResponses: {}", emergencyResponses);
        return emergencyResponses;
    }

    private EmergencyResponse parseEmergencyResponse(JsonNode item) {
        EmergencyResponse emergencyResponse = new EmergencyResponse();
        emergencyResponse.setHpid(item.path("hpid").asText());
        emergencyResponse.setHvidate(item.path("hvidate").asText()); // 입력일시
        emergencyResponse.setHvdnm(item.path("hvdnm").asText()); // 당직의
        emergencyResponse.setHv1(item.path("hv1").asText()); // 응급실 당직의 직통연락처
        emergencyResponse.setDutyName(item.path("dutyName").asText()); // 병원명
        emergencyResponse.setDutyTel3(item.path("dutyTel3").asText()); // 기본정보란

        // 실시간 가용 가능 장비
        emergencyResponse.setHvctayn(item.path("hvctayn").asText()); // CT가용(가/부)
        emergencyResponse.setHvmriayn(item.path("hvmriayn").asText()); // MRI가용(가/부)
        emergencyResponse.setHvangioayn(item.path("hvangioayn").asText()); // 혈관촬영기가용(가/부)
        emergencyResponse.setHvventiayn(item.path("hvventiayn").asText()); // 인공호흡기가용(가/부)
        emergencyResponse.setHvventisoayn(item.path("hvventisoayn").asText()); // 인공호흡기 조산아가용(가/부)
        emergencyResponse.setHvincuayn(item.path("hvincuayn").asText()); // 인큐베이터가용(가/부)
        emergencyResponse.setHvcrrtayn(item.path("hvcrrtayn").asText()); // CRRT가용(가/부)
        emergencyResponse.setHvecmoayn(item.path("hvecmoayn").asText()); // ECMO가용(가/부)
        emergencyResponse.setHvoxyayn(item.path("hvoxyayn").asText()); // 고압산소치료기가용(가/부)
        emergencyResponse.setHvhypoayn(item.path("hvhypoayn").asText()); // 중심체온조절유도기(가/부)
        emergencyResponse.setHvamyn(item.path("hvamyn").asText()); // 구급차가용여부

        // 중환자실
        emergencyResponse.setHvicc(item.path("hvicc").asText()); // [중환자실] 일반
        emergencyResponse.setHv2(item.path("hv2").asText()); // [중환자실] 내과
        emergencyResponse.setHv3(item.path("hv3").asText()); // [중환자실] 외과
        emergencyResponse.setHvcc(item.path("hvcc").asText()); // [중환자실] 신경과
        emergencyResponse.setHvncc(item.path("hvncc").asText()); // [중환자실] 신생아
        emergencyResponse.setHvccc(item.path("hvccc").asText()); // [중환자실] 흉부외과
        emergencyResponse.setHv6(item.path("hv6").asText()); // [중환자실] 신경외과
        emergencyResponse.setHv8(item.path("hv8").asText()); // [중환자실] 화상
        emergencyResponse.setHv9(item.path("hv9").asText()); // [중환자실] 외상
        emergencyResponse.setHv32(item.path("hv32").asText()); // [중환자실] 소아
        emergencyResponse.setHv34(item.path("hv34").asText()); // [중환자실] 심장내과
        emergencyResponse.setHv35(item.path("hv35").asText()); // [중환자실] 음압격리

        // 응급전용
        emergencyResponse.setHv17(item.path("hv17").asText()); // [응급전용] 중환자실 음압격리
        emergencyResponse.setHv18(item.path("hv18").asText()); // [응급전용] 중환자실 일반격리
        emergencyResponse.setHv19(item.path("hv19").asText()); // [응급전용] 입원실 음압격리
        emergencyResponse.setHv21(item.path("hv21").asText()); // [응급전용] 입원실 일반격리
        emergencyResponse.setHv31(item.path("hv31").asText()); // [응급전용] 중환자실
        emergencyResponse.setHv33(item.path("hv33").asText()); // [응급전용] 소아중환자실
        emergencyResponse.setHv36(item.path("hv36").asText()); // [응급전용] 입원실
        emergencyResponse.setHv37(item.path("hv37").asText()); // [응급전용] 소아입원실

        // 입원실
        emergencyResponse.setHvgc(item.path("hvgc").asText()); // [입원실] 일반
        emergencyResponse.setHv41(item.path("hv41").asText()); // [입원실] 음압격리
        emergencyResponse.setHv40(item.path("hv40").asText()); // [입원실] 정신과 폐쇄병동
        emergencyResponse.setHvoc(item.path("hvoc").asText()); // [기타] 수술실
        emergencyResponse.setHv42(item.path("hv42").asText()); // [기타] 분만실
        emergencyResponse.setHv43(item.path("hv43").asText()); // [기타] 화상전용처치실

        // 감염 입원 병상
        emergencyResponse.setHv22(item.path("hv22").asText()); // 감염병 전담병상 중환자실
        emergencyResponse.setHv23(item.path("hv23").asText()); // 감염병 전담병상 중환자실 내 음압격리병상
        emergencyResponse.setHv24(item.path("hv24").asText()); // [감염] 중증 병상
        emergencyResponse.setHv25(item.path("hv25").asText()); // [감염] 준-중증 병상
        emergencyResponse.setHv26(item.path("hv26").asText()); // [감염] 중등증 병상

        // if(hpid == 그 병원) -> 서비스 호출로 emergencyResponse 남은 거 채워서 보내주기

        return emergencyResponse;
    }






}
