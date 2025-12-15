package com.clinic.logic.controller;

import com.clinic.grpc.LoginResponse;
import com.clinic.logic.security.JwtUtil;
import com.clinic.logic.service.DataTierClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataTierClient dataTierClient;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Login with valid credentials returns 200 OK")
    void testLogin_ValidCredentials_ReturnsOk() throws Exception {
        // Arrange
        LoginResponse mockResponse = LoginResponse.newBuilder()
                .setSuccess(true)
                .setUserId(1)
                .setUserType("PATIENT")
                .setName("John Doe")
                .build();

        when(dataTierClient.login(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString()))
                .thenReturn("mock-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@test.com\",\"password\":\"pass123\",\"userType\":\"PATIENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Login with invalid credentials returns 401 Unauthorized")
    void testLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginResponse mockResponse = LoginResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Invalid email or password")
                .build();

        when(dataTierClient.login(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong@test.com\",\"password\":\"wrong\",\"userType\":\"PATIENT\"}"))
                .andExpect(status().isUnauthorized());
    }
}