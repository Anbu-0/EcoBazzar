package com.ecobazaar.ecobazaar.service;

import java.nio.file.FileSystem;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AiService {
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	private final String url = "http://localhost:5000/predict";
	
	
	public Map<String, Object> predictQuality(String imagePath){
		
		try {
			
		
		FileSystemResource resource = new FileSystemResource(imagePath);
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		
		body.add("image", resource);
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
		
		ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
		
		return response.getBody();
		
		}catch(Exception e) {
			throw new RuntimeException("Ai Service call failed: "+e.getMessage());
		}
		
	}

}
