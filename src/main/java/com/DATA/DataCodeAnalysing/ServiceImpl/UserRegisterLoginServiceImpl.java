package com.DATA.DataCodeAnalysing.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.DATA.DataCodeAnalysing.Dto.BaseDTO;
import com.DATA.DataCodeAnalysing.Entity.UserInfo;
import com.DATA.DataCodeAnalysing.Repositary.UserInfoRepositary;
import com.DATA.DataCodeAnalysing.Service.UserRegisterLoginService;
import com.DATA.DataCodeAnalysing.Util.JwtUtil;

@Service
public class UserRegisterLoginServiceImpl implements UserRegisterLoginService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserInfoRepositary userInfoRepositary;

    @Autowired
    private JwtUtil jwtUtil; // Inject JWT utility for token generation and validation

    @Override
    public BaseDTO UserRegistration(UserInfo userInfo) {
        BaseDTO baseDTO = new BaseDTO();

        // Validate input fields
        if (userInfo.getEmail().isEmpty() || userInfo.getUsername().isEmpty() || userInfo.getPassword().isEmpty()) {
            baseDTO.setStatusCode(1);
            baseDTO.setErrorMessage("Please fill all the fields");
            baseDTO.setResponseContent(null);
            return baseDTO;
        }

        // Check if user with the given email or username already exists
        UserInfo userDetailByEmail = userInfoRepositary.findByEmail(userInfo.getEmail());
        UserInfo userDetailByUsername = userInfoRepositary.findByUsername(userInfo.getUsername());

        if (userDetailByEmail != null || userDetailByUsername != null) {
            baseDTO.setStatusCode(1);
            baseDTO.setErrorMessage("User already exists");
            baseDTO.setResponseContent(null);
            return baseDTO;
        }

        // Encrypt the password
        String hashedPassword = bCryptPasswordEncoder.encode(userInfo.getPassword());
        userInfo.setPassword(hashedPassword);

        // Save the new user
        UserInfo savedUser = userInfoRepositary.save(userInfo);

        // Generate JWT token for the newly registered user (optional auto-login after registration)
        String token = jwtUtil.generateToken(savedUser.getUsername());

        // Set response
        baseDTO.setStatusCode(0);
        baseDTO.setErrorMessage("Registration successful");
        baseDTO.setResponseContent(token); // Return token
        return baseDTO;
    }

    @Override
    public BaseDTO UserLogin(UserInfo userInfo) {
        BaseDTO baseDTO = new BaseDTO();

        // Validate input fields
        if (userInfo.getEmail().isEmpty() || userInfo.getPassword().isEmpty()) {
            baseDTO.setStatusCode(1);
            baseDTO.setErrorMessage("Please fill all the fields");
            baseDTO.setResponseContent(null);
            return baseDTO;
        }

        // Fetch user details by email
        UserInfo userDetail = userInfoRepositary.findByEmail(userInfo.getEmail());

        if (userDetail != null) {
            // Check if the password matches
            if (bCryptPasswordEncoder.matches(userInfo.getPassword(), userDetail.getPassword())) {
                // Generate JWT token
                String token = jwtUtil.generateToken(userDetail.getUsername());

                // Return login success with token
                baseDTO.setStatusCode(0);
                baseDTO.setErrorMessage("Login successful");
                baseDTO.setResponseContent(token); // Return JWT token
                return baseDTO;
            } else {
                baseDTO.setStatusCode(1);
                baseDTO.setErrorMessage("Invalid password");
                baseDTO.setResponseContent(null);
                return baseDTO;
            }
        } else {
            baseDTO.setStatusCode(1);
            baseDTO.setErrorMessage("User does not exist");
            baseDTO.setResponseContent(null);
            return baseDTO;
        }
    }

    // Optional: Method to validate JWT token if needed for secured endpoints
    public BaseDTO validateToken(String token) {
        BaseDTO baseDTO = new BaseDTO();

        try {
            String username = jwtUtil.extractUsername(token);
            UserInfo userDetail = userInfoRepositary.findByUsername(username);

            if (userDetail != null && jwtUtil.validateToken(token, userDetail.getUsername())) {
                baseDTO.setStatusCode(0);
                baseDTO.setErrorMessage("Token is valid");
                baseDTO.setResponseContent(userDetail); // Return user details if token is valid
            } else {
                baseDTO.setStatusCode(1);
                baseDTO.setErrorMessage("Invalid or expired token");
                baseDTO.setResponseContent(null);
            }
        } catch (Exception e) {
            baseDTO.setStatusCode(1);
            baseDTO.setErrorMessage("Invalid token");
            baseDTO.setResponseContent(null);
        }

        return baseDTO;
    }
}
