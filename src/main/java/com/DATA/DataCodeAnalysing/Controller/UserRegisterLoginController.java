package com.DATA.DataCodeAnalysing.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DATA.DataCodeAnalysing.Dto.BaseDTO;
import com.DATA.DataCodeAnalysing.Entity.UserInfo;
import com.DATA.DataCodeAnalysing.Service.UserRegisterLoginService;
import com.DATA.DataCodeAnalysing.Util.JwtUtil;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserRegisterLoginController {

    @Autowired
    private UserRegisterLoginService userRegisterLoginService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public BaseDTO UserRegistration(@RequestBody UserInfo userInfo) {
        return userRegisterLoginService.UserRegistration(userInfo);
    }

    @PostMapping("/login")
    public BaseDTO UserLogin(@RequestBody UserInfo userInfo) {
    	System.out.println(userInfo);
        return userRegisterLoginService.UserLogin(userInfo);
    }

    @PostMapping("/validate")
    public BaseDTO validateToken(@RequestHeader("Authorization") String token) {
        BaseDTO response = new BaseDTO();
        try {
            // Remove 'Bearer ' prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                response.setErrorMessage("Token is valid.");
            } else {
                response.setErrorMessage("Token is invalid or expired.");
            }
        } catch (Exception e) {
            response.setErrorMessage("Invalid token.");
        }
        return response;
    }
}
