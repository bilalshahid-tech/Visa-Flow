package com.visaflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visaflow.auth.dto.AuthResponse;
import com.visaflow.auth.dto.LoginRequest;
import com.visaflow.auth.dto.UserDTO;
import com.visaflow.auth.security.JwtService;
import com.visaflow.auth.service.AuthenticationService;
import com.visaflow.auth.service.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for controller tests
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private EmailVerificationService emailVerificationService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturnOk() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "Password123!", false);
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("accessToken")
                .user(UserDTO.builder().email("test@example.com").build())
                .build();

        when(authenticationService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }
}
