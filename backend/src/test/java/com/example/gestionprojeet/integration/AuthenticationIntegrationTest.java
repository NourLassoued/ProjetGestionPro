package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.auth.AuthenticationReponse;
import com.example.gestionprojeet.auth.AuthenticationRequest;
import com.example.gestionprojeet.auth.RegisterRequest;
import com.example.gestionprojeet.classes.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private ObjectMapper objectMapper;


    private RegisterRequest validRegisterRequest;
    private RegisterRequest adminRegisterRequest;


    @BeforeEach
    void setUp() {

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setFirstname("Test User");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setRole(Role.MEMBRE);

        adminRegisterRequest = new RegisterRequest();
        adminRegisterRequest.setFirstname("Admin User");
        adminRegisterRequest.setEmail("admin@example.com");
        adminRegisterRequest.setPassword("adminpass123");
        adminRegisterRequest.setRole(Role.ADMINISTRATEUR);
    }

    // ==================== REGISTRATION FLOW ====================

    @Test
    void testCompleteRegistrationFlow() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refersh_token").isNotEmpty())
                .andExpect(jsonPath("$.access_token", is(notNullValue())))
                .andExpect(jsonPath("$.refersh_token", is(notNullValue())));

        // Vérifier que l'utilisateur a été créé dans la BD
        org.assertj.core.api.Assertions.assertThat(utlisateurRepo.findByEmail("test@example.com")).isPresent();
    }

    @Test
    void testRegisterMultipleUsers() throws Exception {
        // Register User 1
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Register User 2 (Admin)
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegisterRequest)))
                .andExpect(status().isOk());

        // Vérifier que les deux utilisateurs existent
        org.assertj.core.api.Assertions.assertThat(utlisateurRepo.findByEmail("test@example.com")).isPresent();
        org.assertj.core.api.Assertions.assertThat(utlisateurRepo.findByEmail("admin@example.com")).isPresent();
    }

    @Test
    void testRegisterWithMinimumValidData() throws Exception {
        RegisterRequest minRequest = new RegisterRequest();
        minRequest.setFirstname("M");
        minRequest.setEmail("m@test.com");
        minRequest.setPassword("1");
        minRequest.setRole(Role.MEMBRE);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minRequest)))
                .andExpect(status().isOk());
    }

    // ==================== AUTHENTICATION FLOW ====================

    @Test
    void testCompleteAuthenticationFlow() throws Exception {
        // Register
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Authenticate
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }
    @Test
    void testAuthenticateMultipleUsers() throws Exception {
        // Register User 1
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Register User 2
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegisterRequest)))
                .andExpect(status().isOk());

        // Authenticate User 1
        AuthenticationRequest auth1 = new AuthenticationRequest();
        auth1.setEmail("test@example.com");
        auth1.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auth1)))
                .andExpect(status().isOk());

        // Authenticate User 2
        AuthenticationRequest auth2 = new AuthenticationRequest();
        auth2.setEmail("admin@example.com");
        auth2.setPassword("adminpass123");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auth2)))
                .andExpect(status().isOk());
    }

    // ==================== TOKEN REFRESH ====================

    @Test
    void testRefreshTokenWithValidToken() throws Exception {
        // Register
        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthenticationReponse response = objectMapper.readValue(registerResponse, AuthenticationReponse.class);
        String refreshToken = response.getRefershToken();

        // Refresh token
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header("Authorization", "Bearer " + refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRefreshTokenWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ==================== LOGOUT FLOW ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"MEMBRE"})
    void testLogoutWithValidSession() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMINISTRATEUR"})
    void testLogoutAsAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== ERROR HANDLING ====================

    @Test
    void testRegisterWithEmptyJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAuthenticateWithEmptyJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}