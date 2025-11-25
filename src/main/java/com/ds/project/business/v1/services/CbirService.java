package com.ds.project.business.v1.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ds.project.common.utils.InMemoryMultipartFile;
import com.ds.project.common.utils.MultipartInputStreamFileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class CbirService {
//    @Value("${ml.api.url}")
    private String apiUrl = "http://localhost:5000";

    @Autowired
    private GoogleDriveService googleDriveService;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<ImageFeatureResult> extractImagesAndFeatures(MultipartFile file) {
        String url = apiUrl + "/extract-features"; // Gọi route mới
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // Trả về Map JSON
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("images")) {
                List<Map<String, Object>> images = (List<Map<String, Object>>) response.getBody().get("images");
                List<ImageFeatureResult> results = new ArrayList<>();

                for (Map<String, Object> img : images) {
                    String filename = (String) img.get("filename");
                    String urlUploaded = (String) img.get("url");
                    List<Double> features = (List<Double>) img.get("features");

                    results.add(new ImageFeatureResult(filename, urlUploaded, features));
                }
                return results;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }


    public JsonNode searchImage(MultipartFile file) {
        try {
            String url = apiUrl + "/search-image";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error calling Flask API: " + e.getMessage(), e);
        }
    }

    public void pushFeatureToFlask(String id, String productVariantId, String imagePath, Double[] features) {
        String url = apiUrl + "/add-features";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> item = new HashMap<>();
            item.put("id", id);
            item.put("imagePath", imagePath);
            item.put("variantId", productVariantId);
            item.put("featureVector", features);

            Map<String, Object> body = new HashMap<>();
            body.put("items", List.of(item));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to push feature to Flask: " + e.getMessage());
        }
    }

    public static class ImageFeatureResult {
        private String filename;
        private String url;
        private List<Double> features;

        public ImageFeatureResult(String filename, String url, List<Double> features) {
            this.filename = filename;
            this.url = url;
            this.features = features;
        }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public List<Double> getFeatures() { return features; }
        public void setFeatures(List<Double> features) { this.features = features; }
    }

}

