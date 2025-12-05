package com.clinic.logic.controller;

import com.clinic.grpc.LoginResponse;
import com.clinic.grpc.StatusResponse;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private DataTierClient dataTierClient;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginDTO loginDTO) {
        LoginResponse response = dataTierClient.login(
                loginDTO.getEmail(),
                loginDTO.getPassword(),
                loginDTO.getUserType()
        );

        if (response.getSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", response.getUserId());
            data.put("userType", response.getUserType());
            data.put("name", response.getName());
            data.put("token", response.getToken());
            return ResponseEntity.ok(ApiResponse.success("Login successful", data));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterDTO registerDTO) {
        StatusResponse response = dataTierClient.register(
                registerDTO.getName(),
                registerDTO.getEmail(),
                registerDTO.getPhone(),
                registerDTO.getPassword(),
                registerDTO.getUserType(),
                registerDTO.getSpecialization(),
                registerDTO.getRole()
        );

        if (response.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }
}

