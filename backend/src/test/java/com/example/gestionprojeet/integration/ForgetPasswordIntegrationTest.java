package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.ForgetPasswordRepository;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.ChangePassword;
import com.example.gestionprojeet.classes.ForgotPassword;
import com.example.gestionprojeet.classes.Role;
import com.example.gestionprojeet.classes.Utlisateur;
import com.example.gestionprojeet.Service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("INTEGRATION TESTS - ForgetPassword (Complet)")
class ForgetPasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private ForgetPasswordRepository forgetPasswordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private EmailService emailService;

    private Utlisateur testUser;
    private String testEmail;
    private static final String BASE_URL = "/forgetPassword";

    @BeforeEach
    void setup() {

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        forgetPasswordRepository.deleteAll();
        utlisateurRepo.deleteAll();
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");


        testEmail = "john@example.com";
        testUser = Utlisateur.builder()
                .firstname("John Doe")
                .email(testEmail)
                .password(passwordEncoder.encode("oldPassword123"))
                .role(Role.MEMBRE)
                .image("john.jpg")
                .build();
        testUser = utlisateurRepo.save(testUser);


        doNothing().when(emailService).setJavaMailSender(any());
    }

    @Nested
    @DisplayName("Repository Tests")
    class RepositoryTests {

        @Test
        @DisplayName(" Repository: save() - ForgotPassword sauvegardé")
        void test_repository_save() {
            ForgotPassword fp = ForgotPassword.builder()
                    .otp(123456)
                    .expirationTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .build();

            ForgotPassword saved = forgetPasswordRepository.save(fp);

            assertNotNull(saved.getFpid(), "L'ID devrait être généré");
            assertEquals(123456, saved.getOtp());
        }

        @Test
        @DisplayName("Repository: findByOtpAndUtlisateur() - Succès")
        void test_repository_findByOtpAndUtlisateur_success() {
            ForgotPassword fp = ForgotPassword.builder()
                    .otp(654321)
                    .expirationTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .build();
            forgetPasswordRepository.save(fp);

            var found = forgetPasswordRepository.findByOtpAndUtlisateur(654321, testUser);

            assertTrue(found.isPresent(), "ForgotPassword devrait être trouvé");
            assertEquals(654321, found.get().getOtp());
        }

        @Test
        @DisplayName(" Repository: findByOtpAndUtlisateur() - Non trouvé")
        void test_repository_findByOtpAndUtlisateur_notFound() {
            var found = forgetPasswordRepository.findByOtpAndUtlisateur(999999, testUser);

            assertTrue(found.isEmpty(), "ForgotPassword ne devrait pas exister");
        }

        @Test
        @DisplayName("Repository: delete() - ForgotPassword supprimé")
        void test_repository_delete() {
            ForgotPassword fp = ForgotPassword.builder()
                    .otp(111111)
                    .expirationTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .build();
            ForgotPassword saved = forgetPasswordRepository.save(fp);

            forgetPasswordRepository.deleteById(saved.getFpid());

            var found = forgetPasswordRepository.findById(saved.getFpid());
            assertTrue(found.isEmpty(), "ForgotPassword devrait être supprimé");
        }
    }

    @Nested
    @DisplayName("Controller Tests - Succès")
    class ControllerSuccessTests {

        @Test
        @DisplayName(" POST /forgetPassword/verifyMail/{email} - Envoie OTP")
        void test_controller_verifyMail_success() throws Exception {
            mockMvc.perform(post(BASE_URL + "/verifyMail/{email}", testEmail))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Email sent for verification")));

            verify(emailService, times(1)).setJavaMailSender(any());

            assertTrue(forgetPasswordRepository.count() > 0, "ForgotPassword devrait être créé");
        }

        @Test
        @DisplayName(" POST /forgetPassword/verifyMail/{email} - Email inexistant → 404")
        void test_controller_verifyMail_notFound() throws Exception {
            mockMvc.perform(post(BASE_URL + "/verifyMail/{email}", "notexist@example.com"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("valid email")));
        }

        @Test
        @DisplayName(" POST /forgetPassword/verifyOtp/{otp}/{email} - Vérifie OTP")
        void test_controller_verifyOtp_success() throws Exception {
            int otp = 123456;
            ForgotPassword fp = ForgotPassword.builder()
                    .otp(otp)
                    .expirationTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .build();
            forgetPasswordRepository.save(fp);

            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", otp, testEmail))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("OTP verified")));

            var found = forgetPasswordRepository.findByOtpAndUtlisateur(otp, testUser);
            assertTrue(found.isEmpty(), "ForgotPassword devrait être supprimé après vérification");
        }

        @Test
        @DisplayName("POST /forgetPassword/verifyOtp/{otp}/{email} - OTP invalide")
        void test_controller_verifyOtp_invalid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", 999999, testEmail))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid OTP")));
        }

        @Test
        @DisplayName("POST /forgetPassword/verifyOtp/{otp}/{email} - OTP expiré")
        void test_controller_verifyOtp_expired() throws Exception {
            int otp = 654321;
            ForgotPassword fp = ForgotPassword.builder()
                    .otp(otp)
                    .expirationTime(new Date(System.currentTimeMillis() - 1000))
                    .user(testUser)
                    .build();
            forgetPasswordRepository.save(fp);

            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", otp, testEmail))
                    .andExpect(status().isExpectationFailed())
                    .andExpect(content().string(containsString("OTP has expired")));

            var found = forgetPasswordRepository.findByOtpAndUtlisateur(otp, testUser);
            assertTrue(found.isEmpty(), "OTP expiré devrait être supprimé");
        }

        @Test
        @DisplayName(" POST /forgetPassword/changePassword/{email} - Change mot de passe")
        void test_controller_changePassword_success() throws Exception {
            String newPassword = "newPassword456";
            ChangePassword changePassword = new ChangePassword(newPassword, newPassword);

            mockMvc.perform(post(BASE_URL + "/changePassword/{email}", testEmail)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(changePassword)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Password has been changed")));

            Utlisateur updated = utlisateurRepo.findByEmail(testEmail).orElseThrow();
            assertTrue(passwordEncoder.matches(newPassword, updated.getPassword()),
                    "Le password devrait être mis à jour");
        }

        @Test
        @DisplayName(" POST /forgetPassword/changePassword/{email} - Passwords ne correspondent pas")
        void test_controller_changePassword_mismatch() throws Exception {
            ChangePassword changePassword = new ChangePassword("password1", "password2");

            mockMvc.perform(post(BASE_URL + "/changePassword/{email}", testEmail)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(changePassword)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("do not match")));
        }

        @Test
        @DisplayName(" POST /forgetPassword/changePassword/{email} - Email inexistant")
        void test_controller_changePassword_notFound() throws Exception {
            ChangePassword changePassword = new ChangePassword("newPass123", "newPass123");

            mockMvc.perform(post(BASE_URL + "/changePassword/{email}", "notexist@example.com")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(changePassword)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Workflow Tests")
    class WorkflowTests {

        @Test
        @DisplayName("Workflow complet: verifyMail → verifyOtp → changePassword")
        void test_complete_workflow() throws Exception {
            mockMvc.perform(post(BASE_URL + "/verifyMail/{email}", testEmail))
                    .andExpect(status().isOk());

            ForgotPassword fp = forgetPasswordRepository.findAll().stream()
                    .filter(f -> f.getUser().getEmail().equals(testEmail))
                    .findFirst()
                    .orElseThrow();

            int otp = fp.getOtp();

            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", otp, testEmail))
                    .andExpect(status().isOk());

            String newPassword = "newPassword789";
            ChangePassword changePassword = new ChangePassword(newPassword, newPassword);

            mockMvc.perform(post(BASE_URL + "/changePassword/{email}", testEmail)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(changePassword)))
                    .andExpect(status().isOk());

            Utlisateur updated = utlisateurRepo.findByEmail(testEmail).orElseThrow();
            assertTrue(passwordEncoder.matches(newPassword, updated.getPassword()),
                    "Le password devrait être changé");
        }

        @Test
        @DisplayName(" Workflow: Impossible de vérifier OTP deux fois")
        void test_workflow_otp_cannot_verify_twice() throws Exception {
            mockMvc.perform(post(BASE_URL + "/verifyMail/{email}", testEmail))
                    .andExpect(status().isOk());

            ForgotPassword fp = forgetPasswordRepository.findAll().stream()
                    .filter(f -> f.getUser().getEmail().equals(testEmail))
                    .findFirst()
                    .orElseThrow();

            int otp = fp.getOtp();

            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", otp, testEmail))
                    .andExpect(status().isOk());

            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", otp, testEmail))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName(" Public Access Tests")
    class PublicAccessTests {

        @Test
        @DisplayName("POST /forgetPassword/verifyMail/{email} - Accessible sans JWT")
        void test_verifyMail_public() throws Exception {
            mockMvc.perform(post(BASE_URL + "/verifyMail/{email}", testEmail))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" POST /forgetPassword/verifyOtp/{otp}/{email} - Accessible sans JWT")
        void test_verifyOtp_public() throws Exception {
            ForgotPassword fp = ForgotPassword.builder()
                    .otp(111222)
                    .expirationTime(new Date(System.currentTimeMillis() + 70000))
                    .user(testUser)
                    .build();
            forgetPasswordRepository.save(fp);

            mockMvc.perform(post(BASE_URL + "/verifyOtp/{otp}/{email}", 111222, testEmail))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /forgetPassword/changePassword/{email} - Accessible sans JWT")
        void test_changePassword_public() throws Exception {
            ChangePassword changePassword = new ChangePassword("newPass123", "newPass123");

            mockMvc.perform(post(BASE_URL + "/changePassword/{email}", testEmail)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(changePassword)))
                    .andExpect(status().isOk());
        }
    }
}