package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.CarteRepo;
import com.example.gestionprojeet.Respository.MessageRepo;
import com.example.gestionprojeet.Respository.ProjetRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.Token.TokenRepository;
import com.example.gestionprojeet.classes.Role;
import com.example.gestionprojeet.classes.Utlisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("✅ INTEGRATION TESTS - Utilisateur (Complet)")
class UtlisateurIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private TokenRepository tokenRepo;

    @Autowired
    private CarteRepo carteRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ProjetRepo projetRepo;

    @Autowired
    private TokenRepository tableauRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Utlisateur testUser;
    private Utlisateur testUser2;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");

        tokenRepo.deleteAll();
        carteRepo.deleteAll();
        messageRepo.deleteAll();
        projetRepo.deleteAll();
        tableauRepo.deleteAll();
        utlisateurRepo.deleteAll();

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        testUser = Utlisateur.builder()
                .firstname("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.MEMBRE)
                .image("image.jpg")
                .build();
        testUser = utlisateurRepo.save(testUser);

        testUser2 = Utlisateur.builder()
                .firstname("Jane Smith")
                .email("jane@example.com")
                .password("password456")
                .role(Role.ADMINISTRATEUR)
                .image("jane.jpg")
                .build();
        testUser2 = utlisateurRepo.save(testUser2);
    }


    @Nested
    @DisplayName(" Repository Tests")
    class RepositoryTests {

        @Test
        @DisplayName("Repository: findByEmail() - Utilisateur trouvé")
        void test_repository_findByEmail_success() {
            Optional<Utlisateur> found = utlisateurRepo.findByEmail("john@example.com");

            assertTrue(found.isPresent(), "L'utilisateur devrait être trouvé");
            assertEquals("John Doe", found.get().getFirstname());
            assertEquals(Role.MEMBRE, found.get().getRole());
        }

        @Test
        @DisplayName(" Repository: findByEmail() - Utilisateur non trouvé")
        void test_repository_findByEmail_notFound() {
            Optional<Utlisateur> found = utlisateurRepo.findByEmail("inexistant@example.com");

            assertTrue(found.isEmpty(), "L'utilisateur ne devrait pas exister");
        }

        @Test
        @DisplayName("Repository: findAllEmails() - Retourne tous les emails")
        void test_repository_findAllEmails() {
            List<String> emails = utlisateurRepo.findAllEmails();

            assertEquals(2, emails.size(), "Devrait avoir 2 emails");
            assertTrue(emails.contains("john@example.com"));
            assertTrue(emails.contains("jane@example.com"));
        }

        @Test
        @DisplayName(" Repository: updatePassword() - Mot de passe mis à jour")
        void test_repository_updatePassword() {
            utlisateurRepo.updatePassword(testUser.getEmail(), "newPassword456");
            Optional<Utlisateur> updated = utlisateurRepo.findById(testUser.getId());
            assertTrue(updated.isPresent());
            assertEquals("newPassword456", updated.get().getPassword());
        }

        @Test
        @DisplayName(" Repository: save() - Utilisateur sauvegardé")
        void test_repository_save() {
            Utlisateur newUser = Utlisateur.builder()
                    .firstname("Bob Johnson")
                    .email("bob@example.com")
                    .password("bobPassword")
                    .role(Role.MEMBRE)
                    .image("bob.jpg")
                    .build();

            Utlisateur saved = utlisateurRepo.save(newUser);

            assertNotNull(saved.getId(), "L'ID devrait être généré");
            assertEquals("Bob Johnson", saved.getFirstname());

            Optional<Utlisateur> found = utlisateurRepo.findByEmail("bob@example.com");
            assertTrue(found.isPresent());
        }
    }


    @Nested
    @DisplayName(" Controller Tests (avec authentification)")
    class ControllerAuthTests {
        @Test
        void test_controller_saveUser_success() throws Exception {
            Utlisateur newUser = Utlisateur.builder()
                    .firstname("Alice Wonder")
                    .email("alice@example.com")
                    .password("alicePass123")
                    .role(Role.MEMBRE)
                    .image("alice.jpg")
                    .build();

            mockMvc.perform(post("/api/v1/auth/usersave")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newUser)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName(" PUT /updateUser/{id} - Mettre à jour un utilisateur")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_updateUser_success() throws Exception {
            Map<String, String> body = new HashMap<>();
            body.put("firstname", "John Updated");
            body.put("email", "john.updated@example.com");

            mockMvc.perform(put("/api/v1/auth/updateUser/{id}", testUser.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstname", equalTo("John Updated")));
        }

        @Test
        @DisplayName(" PUT /updateUser/{id} - ID inexistant → 500")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_updateUser_notFound() throws Exception {
            Map<String, String> body = new HashMap<>();
            body.put("firstname", "Nobody");

            mockMvc.perform(put("/api/v1/auth/updateUser/{id}", 99999L)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isInternalServerError())  // ← 500
                    .andExpect(content().string(containsString("non trouvé")));
        }

        @Test
        @DisplayName("DELETE /deleteUser/{id} - Supprimer un utilisateur")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_deleteUser_success() throws Exception {
            mockMvc.perform(delete("/api/v1/auth/deleteUser/{id}", testUser.getId()))
                    .andExpect(status().isOk());


            Optional<Utlisateur> deleted = utlisateurRepo.findById(testUser.getId());
            assertTrue(deleted.isEmpty());
        }

        @Test
        @DisplayName("GET /getAllUser - Récupérer tous les utilisateurs")
        @WithMockUser(username = "jane@example.com", roles = "ADMIN")
        void test_controller_getAllUsers_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/getAllUser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
        }
    }

    @Nested
    @DisplayName("uthentication Tests (Vérifier les 403)")
    class AuthenticationTests {

        @Test
        @DisplayName("POST /usersave - Public (pas de JWT requis)")
        void test_auth_saveUser_public() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("firstname", "Public User");
            body.put("email", "public@example.com");
            body.put("password", "publicPass");
            body.put("role", "MEMBRE");
            body.put("image", "public.jpg");

            mockMvc.perform(post("/api/v1/auth/usersave")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("PUT /updateUser/{id} - Sans JWT → 403 Forbidden")
        void test_auth_updateUser_noAuth() throws Exception {
            Map<String, String> body = new HashMap<>();
            body.put("firstname", "Test");

            mockMvc.perform(put("/api/v1/auth/updateUser/{id}", testUser.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" DELETE /deleteUser/{id} - Sans JWT → 403 Forbidden")
        void test_auth_deleteUser_noAuth() throws Exception {
            mockMvc.perform(delete("/api/v1/auth/deleteUser/{id}", testUser.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /getAllUser - Sans JWT → 403 Forbidden")
        void test_auth_getAllUser_noAuth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/getAllUser"))
                    .andExpect(status().isForbidden());
        }
    }
}