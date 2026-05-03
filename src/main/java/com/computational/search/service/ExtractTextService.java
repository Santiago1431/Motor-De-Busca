package com.computational.search.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ExtractTextService {

    @Value("${ocr.service.url:http://localhost:8001/predict}")
    private String ocrServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractTextFromImage(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("latex")) {
                return (String) responseBody.get("latex");
            } else if (responseBody != null && responseBody.containsKey("error")) {
                throw new RuntimeException("OCR Service error: " + responseBody.get("error"));
            }
            
            return "";
        } catch (Exception e) {
            throw new IOException("Failed to call OCR service: " + e.getMessage(), e);
        }
    }
}
