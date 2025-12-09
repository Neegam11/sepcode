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

/**
 * REST Controller for Authentication operations.
 *
 * Endpoints:
 * - POST /api/auth/login    - Authenticate user and get token
 * - POST /api/auth/register - Register new patient account
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final DataTierClient dataTierClient;

    // Constructor injection (best practice over @Autowired field injection)
    public AuthController(DataTierClient dataTierClient) {
        this.dataTierClient = dataTierClient;
    }

    /**
     * POST /api/auth/login
     * Authenticate a user and return a token.
     *
     * @param loginDTO Login credentials (email, password, userType)
     * @return 200 OK with user data and token, or 401 Unauthorized
     */
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

    /**
     * POST /api/auth/register
     * Register a new patient account.
     * Note: Only patients can self-register. Doctors and Staff are pre-configured.
     *
     * @param registerDTO Registration details
     * @return 201 Created on success, or 400 Bad Request / 403 Forbidden
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        // Only patients can self-register. Doctors and Staff are pre-configured by admin.
        String userType = registerDTO.getUserType();
        if (userType != null && (userType.equalsIgnoreCase("DOCTOR") || userType.equalsIgnoreCase("STAFF"))) {
            // 403 Forbidden - user is not allowed to perform this action
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Doctor and Staff accounts are pre-configured by the administrator. Please contact admin for access.")
            );
        }

        StatusResponse response = dataTierClient.register(
                registerDTO.getName(),
                registerDTO.getEmail(),
                registerDTO.getPhone(),
                registerDTO.getPassword(),
                "PATIENT", // Force user type to PATIENT for self-registration
                null,
                null
        );

        if (response.getSuccess()) {
            // 201 Created for successful resource creation
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response.getMessage(), null));
        } else {
            // 409 Conflict if email already exists, otherwise 400 Bad Request
            if (response.getMessage().contains("already registered")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(response.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }
}
