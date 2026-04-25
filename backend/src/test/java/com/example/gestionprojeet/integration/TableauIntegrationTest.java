package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.TableauRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Role;
import com.example.gestionprojeet.classes.Tableau;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;


import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("INTEGRATION TESTS - Tableau (Complet)")
@ActiveProfiles("test")
class TableauIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TableauRepo tableauRepo;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Utlisateur testUser;
    private Utlisateur testUser2;
    private Tableau testTableau;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");

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

        testTableau = Tableau.builder()
                .nom("Tableau Test")
                .description("Description du tableau test")
                .proprietaire(testUser)
                .build();
        testTableau = tableauRepo.save(testTableau);
    }


    @Nested
    @DisplayName("Repository Tests")
    class RepositoryTests {

        @Test
        @DisplayName(" Repository: save() - Tableau sauvegardé")
        void test_repository_save() {
            Tableau newTableau = Tableau.builder()
                    .nom("Nouveau Tableau")
                    .description("Description")
                    .proprietaire(testUser)
                    .build();

            Tableau saved = tableauRepo.save(newTableau);

            assertNotNull(saved.getIdTableau(), "L'ID devrait être généré");
            assertEquals("Nouveau Tableau", saved.getNom());
            assertEquals(testUser.getId(), saved.getProprietaire().getId());
        }

        @Test
        @DisplayName("Repository: findById() - Tableau trouvé")
        void test_repository_findById_success() {
            var found = tableauRepo.findById(testTableau.getIdTableau());

            assertTrue(found.isPresent(), "Le tableau devrait être trouvé");
            assertEquals("Tableau Test", found.get().getNom());
            assertEquals(testUser.getId(), found.get().getProprietaire().getId());
        }

        @Test
        @DisplayName(" Repository: findById() - Tableau non trouvé")
        void test_repository_findById_notFound() {
            var found = tableauRepo.findById(99999L);

            assertTrue(found.isEmpty(), "Le tableau ne devrait pas exister");
        }

        @Test
        @DisplayName(" Repository: findAll() - Récupère tous les tableaux")
        void test_repository_findAll() {
            Tableau tableau2 = Tableau.builder()
                    .nom("Tableau 2")
                    .description("Description 2")
                    .proprietaire(testUser2)
                    .build();
            tableauRepo.save(tableau2);

            List<Tableau> all = tableauRepo.findAll();

            assertEquals(2, all.size(), "Devrait avoir 2 tableaux");
        }

        @Test
        @DisplayName(" Repository: delete() - Tableau supprimé")
        void test_repository_delete() {
            tableauRepo.deleteById(testTableau.getIdTableau());

            var found = tableauRepo.findById(testTableau.getIdTableau());
            assertTrue(found.isEmpty(), "Le tableau devrait être supprimé");
        }
    }


    @Nested
    @DisplayName(" Controller Tests - Succès")
    class ControllerSuccessTests {

        @Test
        @DisplayName("POST /tableaux/{id} - Créer un tableau")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_createTableau_success() throws Exception {
            Tableau newTableau = Tableau.builder()
                    .nom("Nouveau Tableau")
                    .description("Description du nouveau")
                    .build();

            mockMvc.perform(post("/api/v1/auth/tableaux/{id}", testUser.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newTableau)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nom", equalTo("Nouveau Tableau")))
                    .andExpect(jsonPath("$.description", equalTo("Description du nouveau")));
        }

        @Test
        @DisplayName(" POST /tableaux/byEmail/{email} - Créer un tableau par email")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_createTableauByEmail_success() throws Exception {
            Tableau newTableau = Tableau.builder()
                    .nom("Tableau Par Email")
                    .description("Créé par email")
                    .build();

            mockMvc.perform(post("/api/v1/auth/tableaux/byEmail/{email}", "john@example.com")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newTableau)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nom", equalTo("Tableau Par Email")));
        }

        @Test
        @DisplayName(" GET /tableaux/utilisateur/{id} - Récupérer les tableaux")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_getTableauxByUtilisateur_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/utilisateur/{id}", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].nom", equalTo("Tableau Test")));
        }

        @Test
        @DisplayName(" GET /tableaux/byEmail/{email} - Récupérer les tableaux par email")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_getTableauxByEmail_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/byEmail/{email}", "john@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].nom", equalTo("Tableau Test")));
        }

        @Test
        @DisplayName(" GET /tableaux/utilisateur/{id} - ID inexistant → 500")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_getTableauxByUtilisateur_notFound() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/utilisateur/{id}", 99999L))
                    .andExpect(status().is5xxServerError());
        }
    }


    @Nested
    @DisplayName(" Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName(" POST /tableaux/{id} - Sans JWT → 403 Forbidden")
        void test_auth_createTableau_without_auth() throws Exception {
            Tableau newTableau = Tableau.builder()
                    .nom("Test Tableau")
                    .description("Test")
                    .build();

            mockMvc.perform(post("/api/v1/auth/tableaux/{id}", testUser.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newTableau)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" POST /tableaux/{id} - Avec JWT → 201 Created")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_createTableau_with_auth() throws Exception {
            Tableau newTableau = Tableau.builder()
                    .nom("Test Tableau Auth")
                    .description("Test avec auth")
                    .build();

            mockMvc.perform(post("/api/v1/auth/tableaux/{id}", testUser.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newTableau)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName(" POST /tableaux/byEmail/{email} - Sans JWT → 403 Forbidden")
        void test_auth_createTableauByEmail_without_auth() throws Exception {
            Tableau newTableau = Tableau.builder()
                    .nom("Test")
                    .description("Test")
                    .build();

            mockMvc.perform(post("/api/v1/auth/tableaux/byEmail/{email}", "john@example.com")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newTableau)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" POST /tableaux/byEmail/{email} - Avec JWT → 201 Created")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_createTableauByEmail_with_auth() throws Exception {
            Tableau newTableau = Tableau.builder()
                    .nom("Test By Email Auth")
                    .description("Test")
                    .build();

            mockMvc.perform(post("/api/v1/auth/tableaux/byEmail/{email}", "john@example.com")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newTableau)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName(" GET /tableaux/utilisateur/{id} - Sans JWT → 403 Forbidden")
        void test_auth_getTableauxByUtilisateur_without_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/utilisateur/{id}", testUser.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /tableaux/utilisateur/{id} - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_getTableauxByUtilisateur_with_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/utilisateur/{id}", testUser.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" GET /tableaux/byEmail/{email} - Sans JWT → 403 Forbidden")
        void test_auth_getTableauxByEmail_without_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/byEmail/{email}", "john@example.com"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /tableaux/byEmail/{email} - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_getTableauxByEmail_with_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/byEmail/{email}", "john@example.com"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" Admin peut accéder à tous les endpoints")
        @WithMockUser(username = "jane@example.com", roles = "ADMINISTRATEUR")
        void test_auth_admin_access() throws Exception {
            mockMvc.perform(get("/api/v1/auth/tableaux/utilisateur/{id}", testUser2.getId()))
                    .andExpect(status().isOk());
        }
    }
}