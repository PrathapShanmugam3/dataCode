package com.DATA.DataCodeAnalysing.Service;

import org.springframework.stereotype.Service;

@Service
public interface FirebaseAuthService {

	public String sendOtp(String phoneNumber);

}
