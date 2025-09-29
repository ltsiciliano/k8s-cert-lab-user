package com.example.userservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private static final Logger log = LoggerFactory.getLogger(ExternalApiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${TAX_API_ENABLED:false}")
    private boolean taxApiEnabled;

    @Value("${TAX_API_URL:}")
    private String taxApiUrl;

    @Value("${PAYMENTS_API_ENABLED:false}")
    private boolean paymentsApiEnabled;

    @Value("${PAYMENTS_API_URL:}")
    private String paymentsApiUrl;

    public JsonNode fetchImpostos(String cpf) {
        if (!taxApiEnabled || !StringUtils.hasText(taxApiUrl)) {
            return JsonNodeFactory.instance.arrayNode();
        }
        try {
            String url = appendCpfParam(taxApiUrl, cpf);
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            return toArrayNode(resp.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch impostos from TAX_API_URL: {}", e.toString());
            return JsonNodeFactory.instance.arrayNode();
        }
    }

    public JsonNode fetchPagamentos(String cpf) {
        if (!paymentsApiEnabled || !StringUtils.hasText(paymentsApiUrl)) {
            return JsonNodeFactory.instance.arrayNode();
        }
        try {
            String url = appendCpfParam(paymentsApiUrl, cpf);
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            return toArrayNode(resp.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch pagamentos from PAYMENTS_API_URL: {}", e.toString());
            return JsonNodeFactory.instance.arrayNode();
        }
    }

    private String appendCpfParam(String baseUrl, String cpf) {
        if (baseUrl.contains("?")) {
            return baseUrl + "&cpf=" + cpf;
        }
        return baseUrl + "?cpf=" + cpf;
    }

    private ArrayNode toArrayNode(String body) {
        if (!StringUtils.hasText(body)) {
            return JsonNodeFactory.instance.arrayNode();
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.isArray()) {
                return (ArrayNode) node;
            }
            ArrayNode arr = JsonNodeFactory.instance.arrayNode();
            arr.add(node);
            return arr;
        } catch (Exception e) {
            // If the response is not valid JSON, return empty array.
            return JsonNodeFactory.instance.arrayNode();
        }
    }
}
