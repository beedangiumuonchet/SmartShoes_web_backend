package com.ds.project.business.v1.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${python.api.update-all}")   // http://localhost:8000/update-all
    private String updateAllUrl;

    @Value("${python.api.delete}")       // http://localhost:8000/delete
    private String deleteUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    // Call Python: Update all embeddings
    public void updateAllEmbeddings() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                updateAllUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
    }


    // Call Python: Delete 1 embedding
    public void deleteEmbedding(String productId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", productId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                entity,
                String.class
        );
    }
}
