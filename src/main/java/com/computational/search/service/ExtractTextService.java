package com.computational.search.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class ExtractTextService {

    private static final String LATEX_KEY = "latex";
    private static final String ERROR_KEY = "error";

    private final String ocrServiceUrl;
    private final RestTemplate restTemplate;

    public ExtractTextService(@Value("${ocr.service.url:http://localhost:8001/predict}") String ocrServiceUrl, 
                               RestTemplate restTemplate) {
        this.ocrServiceUrl = ocrServiceUrl;
        this.restTemplate = restTemplate;
    }

    public String extractTextFromImage(MultipartFile file) throws IOException {
        log.info("Sending image to OCR service: {}", file.getOriginalFilename());

        MultiValueMap<String, Object> body = createMultipartBody(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, Map.class);
            return handleResponse(response);
        } catch (Exception e) {
            log.error("Failed to call OCR service at {}. Error: {}", ocrServiceUrl, e.getMessage());
            throw new IOException("OCR service communication failure", e);
        }
    }

    private MultiValueMap<String, Object> createMultipartBody(MultipartFile file) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        return body;
    }

    private String handleResponse(ResponseEntity<Map> response) {
        Map<String, Object> responseBody = response.getBody();
        
        if (responseBody == null) {
            log.warn("OCR service returned empty body");
            return "";
        }

        if (responseBody.containsKey(LATEX_KEY)) {
            String latex = (String) responseBody.get(LATEX_KEY);
            log.debug("OCR service returned LaTeX: {}", latex);
            return latex;
        } 
        
        if (responseBody.containsKey(ERROR_KEY)) {
            String error = (String) responseBody.get(ERROR_KEY);
            log.error("OCR service returned error: {}", error);
            throw new RuntimeException("OCR Service error: " + error);
        }
        
        return "";
    }
}
