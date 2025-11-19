package com.ds.project.business.v1.services;

import com.ds.project.common.entities.dto.request.AiSearchRequest;
import com.ds.project.common.entities.dto.response.AiSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class AiSearchService {

    @Value("${python.api.url}")   // ví dụ: http://localhost:8000/search
    private String pythonApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AiSearchResponse searchAi(AiSearchRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiSearchRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<AiSearchResponse> response = restTemplate.exchange(
                pythonApiUrl,
                HttpMethod.POST,
                entity,
                AiSearchResponse.class
        );

        return response.getBody();
    }
}
