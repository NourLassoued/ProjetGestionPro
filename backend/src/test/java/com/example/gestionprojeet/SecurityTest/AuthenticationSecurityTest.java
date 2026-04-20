package com.example.gestionprojeet.SecurityTest;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.auth.RegisterRequest;
import com.example.gestionprojeet.classes.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthenticationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private CarteRepo carteRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ProjetRepo projetRepo;

    @Autowired
    private TableauRepo tableauRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        // Delete in correct order to respect foreign key constraints
        carteRepo.deleteAll();
        messageRepo.deleteAll();

        // Delete junction table using SQL
        jdbcTemplate.execute("DELETE FROM utilisateur_projet");

        projetRepo.deleteAll();
        tableauRepo.deleteAll();
        utlisateurRepo.deleteAll();

        validRegisterRequest = RegisterRequest.builder()
                .firstname("John")
                .email("john@test.com")
                .password("password123")
                .build();
    }

    // ✅ TEST 1: Register with valid data
    @Test
    void testRegisterWithValidData_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty());
    }

    // ✅ TEST 2: Response contains required fields
    @Test
    void testRegisterResponse_ContainsRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isString())
                .andExpect(jsonPath("$.refresh_token").isString());
    }

    // ✅ TEST 3: Duplicate email returns 409
    @Test
    void testRegisterWithDuplicateEmail_ReturnsConflict() throws Exception {
        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Second registration with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ✅ TEST 4: Multiple users with different emails
    @Test
    void testRegisterMultipleUsersWithDifferentEmails() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        RegisterRequest secondUser = RegisterRequest.builder()
                .firstname("Jane")
                .email("jane@test.com")
                .password("password456")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isOk());
    }

    // ✅ TEST 5: Empty firstname returns 400
    @Test
    void testRegisterWithEmptyFirstname_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .firstname("")
                .email("john@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ✅ TEST 6: Very long firstname returns 400
    @Test
    void testRegisterWithVeryLongFirstname_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .firstname("a".repeat(100))
                .email("john@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ✅ TEST 7: Empty password returns 400
    @Test
    void testRegisterWithEmptyPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .firstname("John")
                .email("john@test.com")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ✅ TEST 8: Short password returns 400
    @Test
    void testRegisterWithShortPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .firstname("John")
                .email("john@test.com")
                .password("12345")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ✅ TEST 9: JWT tokens generated correctly
    @Test
    void testRegisterGeneratesValidJWT() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists());
    }

    // ✅ TEST 10: With optional fields
    @Test
    void testRegisterWithOptionalFields_Returns200() throws Exception {
        RegisterRequest requestWithOptional = RegisterRequest.builder()
                .firstname("John")
                .email("john.optional@test.com")
                .password("password123")
                .image("https://example.com/image.jpg")
                .role(Role.valueOf("MEMBRE"))
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithOptional)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    void testRegisterWithoutOptionalFields_Returns200() throws Exception {
        RegisterRequest requestWithoutOptional = RegisterRequest.builder()
                .firstname("Jane")
                .email("jane.simple@test.com")
                .password("password456")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutOptional)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    void testRegisterWithSpecialCharactersInFirstname_Returns200() throws Exception {
        RegisterRequest specialRequest = RegisterRequest.builder()
                .firstname("Jean-Pierre")
                .email("jean@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    void testRegisterCreatesUserInDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        var users = utlisateurRepo.findAll();
        assert users.size() > 0 : "User should be created in database";
    }

    @Test
    void testMultipleRegistrations_AllSucceed() throws Exception {
        for (int i = 0; i < 3; i++) {
            RegisterRequest request = RegisterRequest.builder()
                    .firstname("User" + i)
                    .email("user" + i + "@test.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        var users = utlisateurRepo.findAll();
        assert users.size() == 3 : "All 3 users should be created";
    }
}