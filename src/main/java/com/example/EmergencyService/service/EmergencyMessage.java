package com.example.EmergencyService.service;

import com.example.EmergencyService.dto.EmergencyMessageRequest;
import com.example.EmergencyService.dto.EmergencyMessageResponse;
import com.example.EmergencyService.dto.EmergencyRequest;
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
public class EmergencyMessage {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.service.key}")
    private String SERVICE_KEY;

    private static final Logger logger = LoggerFactory.getLogger(EmergencyService.class);

    private static final String PUBLIC_DATA_API_URL = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmSrsillDissMsgInqire";
    private static final String NUM_OF_ROWS = "500";
    private static final String PAGE_NO = "1";

    public List<EmergencyMessageResponse> getMessage(String hpid) { // api 호출 보내기 함수
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        String apiUrl = buildApiUrl(hpid);
        logger.info("Constructed API URL: " + apiUrl);
        String response = restTemplate.getForObject(apiUrl.replace("%25","%"), String.class); // 혹시 몰라서 한번 더 25 제거
        String utf8EncodedResponse = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8); // 한글 깨지는거 잡기

        logger.info("Response: " + utf8EncodedResponse);

        return parseXmlResponse(utf8EncodedResponse);
    }

    private String buildApiUrl(String hpid) { // api 호출 url 만들기

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(PUBLIC_DATA_API_URL)
                .queryParam("pageNo", PAGE_NO)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("HPID", hpid)
                .encode()
                .build();

        StringBuilder apiUrl = new StringBuilder(builder.toString());

        String apiUrlStr = apiUrl.toString();
        logger.info("Final API URL: " + apiUrlStr);
        return apiUrlStr;
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<EmergencyMessageResponse> parseXmlResponse(String response) {
        List<EmergencyMessageResponse> emergencyMessageResponses = new ArrayList<>();
        try {
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(response.getBytes(StandardCharsets.UTF_8));
            JsonNode items = root.path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    EmergencyMessageResponse emergencyMessageResponse = new EmergencyMessageResponse();
                    emergencyMessageResponse.setHpid(item.path("hpid").asText());
                    emergencyMessageResponse.setSymBlkMsg(item.path("symBlkMsg").asText());
                    emergencyMessageResponse.setSymBlkMsgTyp(item.path("symBlkMsgTyp").asText());
                    emergencyMessageResponse.setSymBlkSttDtm(item.path("symBlkSttDtm").asText());
                    emergencyMessageResponse.setSymBlkEndDtm(item.path("symBlkEndDtm").asText());

                    emergencyMessageResponses.add(emergencyMessageResponse);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing XML response", e);
        }
        return emergencyMessageResponses;
    }


}
