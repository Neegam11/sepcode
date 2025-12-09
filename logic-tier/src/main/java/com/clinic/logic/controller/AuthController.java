package com.clinic.logic.controller;

import com.clinic.grpc.LoginResponse;
import com.clinic.grpc.StatusResponse;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final DataTierClient dataTierClient;

    public AuthController(DataTierClient dataTierClient) {
        this.dataTierClient = dataTierClient;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginDTO loginDTO) {
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
            // 401 Unauthorized for invalid credentials (not 400 Bad Request)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(response.getMessage()));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        String userType = registerDTO.getUserType();
        if (userType != null && (userType.equalsIgnoreCase("DOCTOR") || userType.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Doctor and Staff accounts are pre-configured by the administrator. Please contact admin for access.")
            );
        }

        StatusResponse response = dataTierClient.register(
                registerDTO.getName(),
                registerDTO.getEmail(),
                registerDTO.getPhone(),
                registerDTO.getPassword(),
                "PATIENT",
                null,
                null
        );

        if (response.getSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response.getMessage(), null));
        } else {
            if (response.getMessage().contains("already registered")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(response.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }
}
