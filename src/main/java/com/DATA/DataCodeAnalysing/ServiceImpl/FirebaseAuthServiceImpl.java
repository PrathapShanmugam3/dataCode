package com.DATA.DataCodeAnalysing.ServiceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.DATA.DataCodeAnalysing.Service.FirebaseAuthService;

@Service
public class FirebaseAuthServiceImpl implements FirebaseAuthService {

	@Override
	public String sendOtp(String phoneNumber) {
		 final String FIREBASE_API_KEY = "AIzaSyAjpCNLRgPmIWHcf7JxkpR601ZNch_Q6VQ";
	 final String FIREBASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:sendVerificationCode?key=" + FIREBASE_API_KEY;


	        RestTemplate restTemplate = new RestTemplate();
	        
	        Map<String, Object> request = new HashMap<>();
	        request.put("phoneNumber", phoneNumber);
	      

	        try {
	            Map<String, Object> response = restTemplate.postForObject(FIREBASE_URL, request, Map.class);
	            return "OTP sent successfully. Session info: " + response.get("sessionInfo");
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "Failed to send OTP: " + e.getMessage();
	        }
	    }

}
