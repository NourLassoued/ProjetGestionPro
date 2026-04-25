package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.CarteRepo;
import com.example.gestionprojeet.Respository.ProjetRepo;
import com.example.gestionprojeet.Respository.TableauRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("INTEGRATION TESTS - Projet (Complet)")
class ProjetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjetRepo projetRepo;

    @Autowired
    private TableauRepo tableauRepo;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private CarteRepo carteRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Utlisateur testUser;
    private Utlisateur testUser2;
    private Tableau testTableau;
    private Projet testProjet;
    private Carte testCarte;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");

        carteRepo.deleteAll();
        projetRepo.deleteAll();
        tableauRepo.deleteAll();
        utlisateurRepo.deleteAll();

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        testUser = Utlisateur.builder()
                .firstname("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.ADMINISTRATEUR)
                .image("image.jpg")
                .build();
        testUser = utlisateurRepo.save(testUser);

        testUser2 = Utlisateur.builder()
                .firstname("Jane Smith")
                .email("jane@example.com")
                .password("password456")
                .role(Role.MEMBRE)
                .image("jane.jpg")
                .build();
        testUser2 = utlisateurRepo.save(testUser2);

        testTableau = Tableau.builder()
                .nom("Tableau Test")
                .description("Description du tableau")
                .proprietaire(testUser)
                .build();
        testTableau = tableauRepo.save(testTableau);

        testProjet = Projet.builder()
                .nom("Projet Test")
                .description("Description du projet")
                .tableau(testTableau)
                .build();
        testProjet = projetRepo.save(testProjet);

        testCarte = Carte.builder()
                .titre("Carte Test")
                .description("Description de la carte")
                .dateCreation(new Date())
                .statut(StatutCarte.A_FAIRE)
                .projet(testProjet)
                .auteur(testUser)
                .build();
        testCarte = carteRepo.save(testCarte);
    }


    @Nested
    @DisplayName("Repository Tests")
    class RepositoryTests {

        @Test
        @DisplayName(" Repository: save() - Projet sauvegardé")
        void test_repository_save() {
            Projet newProjet = Projet.builder()
                    .nom("Nouveau Projet")
                    .description("Description")
                    .tableau(testTableau)
                    .build();

            Projet saved = projetRepo.save(newProjet);

            assertNotNull(saved.getIdProjet(), "L'ID devrait être généré");
            assertEquals("Nouveau Projet", saved.getNom());
            assertEquals(testTableau.getIdTableau(), saved.getTableau().getIdTableau());
        }

        @Test
        @DisplayName("Repository: findById() - Projet trouvé")
        void test_repository_findById_success() {
            var found = projetRepo.findById(testProjet.getIdProjet());

            assertTrue(found.isPresent(), "Le projet devrait être trouvé");
            assertEquals("Projet Test", found.get().getNom());
        }

        @Test
        @DisplayName("Repository: findById() - Projet non trouvé")
        void test_repository_findById_notFound() {
            var found = projetRepo.findById(99999L);

            assertTrue(found.isEmpty(), "Le projet ne devrait pas exister");
        }

        @Test
        @DisplayName(" Repository: findAll() - Récupère tous les projets")
        void test_repository_findAll() {
            Projet projet2 = Projet.builder()
                    .nom("Projet 2")
                    .description("Description 2")
                    .tableau(testTableau)
                    .build();
            projetRepo.save(projet2);

            List<Projet> all = projetRepo.findAll();

            assertEquals(2, all.size(), "Devrait avoir 2 projets");
        }

        @Test
        @DisplayName("Repository: findByUtilisateursId() - Projets d'un utilisateur")
        void test_repository_findByUtilisateursId() {
            if (testProjet.getUtilisateurs() == null) {
                testProjet.setUtilisateurs(new ArrayList<>());
            }
            if (testUser.getProjets() == null) {
                testUser.setProjets(new ArrayList<>());
            }

            testProjet.getUtilisateurs().add(testUser);
            testUser.getProjets().add(testProjet);

            projetRepo.save(testProjet);
            utlisateurRepo.save(testUser);

            List<Projet> projets = projetRepo.findByUtilisateursId(testUser.getId());

            assertFalse(projets.isEmpty(), "L'utilisateur devrait avoir des projets");
            assertEquals(1, projets.size(), "Devrait avoir 1 projet");
        }

        @Test
        @DisplayName(" Repository: delete() - Projet supprimé")
        void test_repository_delete() {
            projetRepo.deleteById(testProjet.getIdProjet());

            var found = projetRepo.findById(testProjet.getIdProjet());
            assertTrue(found.isEmpty(), "Le projet devrait être supprimé");
        }
    }


    @Nested
    @DisplayName("Controller Tests - Succès")
    class ControllerSuccessTests {

        @Test
        @DisplayName(" POST /{idTableau} - Créer un projet")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_createProject_success() throws Exception {
            Projet newProjet = Projet.builder()
                    .nom("Nouveau Projet")
                    .description("Description")
                    .build();

            mockMvc.perform(post("/api/v1/auth/{idTableau}", testTableau.getIdTableau())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newProjet)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nom", equalTo("Nouveau Projet")));
        }

        @Test
        @DisplayName("ET /getAllProjects - Récupérer tous les projets")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_getAllProjects_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/getAllProjects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].nom", equalTo("Projet Test")));
        }

        @Test
        @DisplayName(" GET /{id} - Récupérer un projet par ID")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_getProjetById_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}", testProjet.getIdProjet()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom", equalTo("Projet Test")))
                    .andExpect(jsonPath("$.description", equalTo("Description du projet")));
        }

        @Test
        @DisplayName(" GET /{id} - ID inexistant → 500")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_getProjetById_notFound() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}", 99999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /{email}/projets/{idProjet} - Ajouter utilisateur au projet")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_ajouterUtilisateurAuProjet_success() throws Exception {
            mockMvc.perform(post("/api/v1/auth/{email}/projets/{idProjet}",
                            "jane@example.com", testProjet.getIdProjet()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom", equalTo("Projet Test")));
        }

        @Test
        @DisplayName("POST /{idProjet}/cartes - Ajouter une carte")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_ajouterCarteAProjet_success() throws Exception {
            Carte newCarte = Carte.builder()
                    .titre("Nouvelle Carte")
                    .description("Description")
                    .dateCreation(new Date())
                    .statut(StatutCarte.A_FAIRE)
                    .build();

            mockMvc.perform(post("/api/v1/auth/{idProjet}/cartes", testProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newCarte)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.titre", equalTo("Nouvelle Carte")));
        }

        @Test
        @DisplayName("GET /{id}/projets - Récupérer les projets d'un utilisateur")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_getProjetsUtilisateur_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}/projets", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
        }

        @Test
        @DisplayName(" GET /{idProjet}/cartes - Récupérer les cartes d'un projet")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_controller_getCartesDuProjet_success() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{idProjet}/cartes", testProjet.getIdProjet()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].titre", equalTo("Carte Test")));
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName(" POST /create/{idTableau} - Sans JWT → 403 Forbidden")
        void test_auth_createProject_without_auth() throws Exception {
            Projet newProjet = Projet.builder()
                    .nom("Test")
                    .description("Test")
                    .build();

            mockMvc.perform(post("/api/v1/auth/create/{idTableau}", testTableau.getIdTableau())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newProjet)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" POST /create/{idTableau} - Avec JWT → 201 Created")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_auth_createProject_with_auth() throws Exception {
            Projet newProjet = Projet.builder()
                    .nom("Test Auth")
                    .description("Test")
                    .build();

            mockMvc.perform(post("/api/v1/auth/{idTableau}", testTableau.getIdTableau())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newProjet)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName(" GET /getAllProjects - Sans JWT → 403 Forbidden")
        void test_auth_getAllProjects_without_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/getAllProjects"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /getAllProjects - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_auth_getAllProjects_with_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/getAllProjects"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" GET /{id} - Sans JWT → 403 Forbidden")
        void test_auth_getProjetById_without_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}", testProjet.getIdProjet()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /{id} - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_getProjetById_with_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}", testProjet.getIdProjet()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" POST /{email}/projets/{idProjet} - Sans JWT → 403 Forbidden")
        void test_auth_ajouterUtilisateur_without_auth() throws Exception {
            mockMvc.perform(post("/api/v1/auth/{email}/projets/{idProjet}",
                            "jane@example.com", testProjet.getIdProjet()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" POST /{email}/projets/{idProjet} - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_auth_ajouterUtilisateur_with_auth() throws Exception {
            mockMvc.perform(post("/api/v1/auth/{email}/projets/{idProjet}",
                            "jane@example.com", testProjet.getIdProjet()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" POST /{idProjet}/cartes - Sans JWT → 403 Forbidden")
        void test_auth_ajouterCarte_without_auth() throws Exception {
            Carte carte = Carte.builder()
                    .titre("Test")
                    .description("Test")
                    .dateCreation(new Date())
                    .statut(StatutCarte.A_FAIRE)
                    .build();

            mockMvc.perform(post("/api/v1/auth/{idProjet}/cartes", testProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(carte)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" POST /{idProjet}/cartes - Avec JWT → 201 Created")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_ajouterCarte_with_auth() throws Exception {
            Carte carte = Carte.builder()
                    .titre("Test Auth")
                    .description("Test")
                    .dateCreation(new Date())
                    .statut(StatutCarte.A_FAIRE)
                    .build();

            mockMvc.perform(post("/api/v1/auth/{idProjet}/cartes", testProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(carte)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName(" GET /{id}/projets - Sans JWT → 403 Forbidden")
        void test_auth_getProjetsUtilisateur_without_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}/projets", testUser.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /{id}/projets - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_auth_getProjetsUtilisateur_with_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{id}/projets", testUser.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" GET /{idProjet}/cartes - Sans JWT → 403 Forbidden")
        void test_auth_getCartesDuProjet_without_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{idProjet}/cartes", testProjet.getIdProjet()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" GET /{idProjet}/cartes - Avec JWT → 200 OK")
        @WithMockUser(username = "john@example.com", roles = "MEMBRE")
        void test_auth_getCartesDuProjet_with_auth() throws Exception {
            mockMvc.perform(get("/api/v1/auth/{idProjet}/cartes", testProjet.getIdProjet()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName(" Admin peut accéder à tous les endpoints")
        @WithMockUser(username = "jane@example.com", roles = "ADMINISTRATEUR")
        void test_auth_admin_access() throws Exception {
            mockMvc.perform(get("/api/v1/auth/getAllProjects"))
                    .andExpect(status().isOk());
        }

    }

    @Nested
    @DisplayName("Controller Tests - Erreurs")
    class ControllerErrorTests {

        @Test
        @DisplayName("DELETE /{id} - Supprimer un projet")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_deleteProject_success() throws Exception {
            mockMvc.perform(delete("/api/v1/auth/{id}", testProjet.getIdProjet()))
                    .andExpect(status().isOk());

            // Vérifier que le projet est supprimé
            assertFalse(projetRepo.existsById(testProjet.getIdProjet()),
                    "Le projet devrait être supprimé");
        }

        @Test
        @DisplayName("PUT /{id} - Modifier un projet")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_updateProject_success() throws Exception {
            Projet updateProjet = Projet.builder()
                    .nom("Projet Modifié")
                    .description("Description modifiée")
                    .build();

            mockMvc.perform(put("/api/v1/auth/{id}", testProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateProjet)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom", equalTo("Projet Modifié")))
                    .andExpect(jsonPath("$.description",
                            equalTo("Description modifiée")));
        }

        @Test
        @DisplayName("POST /create/{idTableau} - Tableau inexistant")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_createProject_tableauNotFound() throws Exception {
            Projet newProjet = Projet.builder()
                    .nom("Test")
                    .description("Test")
                    .build();

            mockMvc.perform(post("/api/v1/auth/create/{idTableau}", 99999L)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newProjet)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("PUT /{id} - Projet inexistant")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_updateProject_notFound() throws Exception {
            Projet updateProjet = Projet.builder()
                    .nom("Test")
                    .description("Test")
                    .build();

            mockMvc.perform(put("/api/v1/auth/{id}", 99999L)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateProjet)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("DELETE /{id} - Projet inexistant")
        @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
        void test_controller_deleteProject_notFound() throws Exception {
            mockMvc.perform(delete("/api/v1/auth/{id}", 99999L))
                    .andExpect(status().is4xxClientError());
        }


        @Nested
        @DisplayName("Authorization Tests (Permissions)")
        class AuthorizationTests {

            @Test
            @DisplayName("Member NE PEUT PAS créer projet (Admin only)")
            @WithMockUser(username = "john@example.com", roles = "MEMBRE")
            void test_auth_createProject_memberCannotCreate() throws Exception {
                Projet newProjet = Projet.builder()
                        .nom("Test")
                        .description("Test")
                        .build();

                mockMvc.perform(post("/api/v1/auth/{idTableau}",
                                testTableau.getIdTableau())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(newProjet)))
                        .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName(" Member NE PEUT PAS modifier (Admin only)")
            @WithMockUser(username = "john@example.com", roles = "MEMBRE")
            void test_auth_updateProject_memberCannotUpdate() throws Exception {
                Projet updateProjet = Projet.builder()
                        .nom("Modifié")
                        .description("Modifié")
                        .build();

                mockMvc.perform(put("/api/v1/auth/{id}", testProjet.getIdProjet())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(updateProjet)))
                        .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName(" Member NE PEUT PAS supprimer (Admin only)")
            @WithMockUser(username = "john@example.com", roles = "MEMBRE")
            void test_auth_deleteProject_memberCannotDelete() throws Exception {
                mockMvc.perform(delete("/api/v1/auth/{id}", testProjet.getIdProjet()))
                        .andExpect(status().isForbidden());
            }

            @Test
            @WithMockUser(username = "john@example.com", roles = "ADMINISTRATEUR")
            void test_auth_admin_canDoEverything() throws Exception {
                //  DÉBOGUER: Voir les autorités réelles
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                Projet newProjet = Projet.builder()
                        .nom("Admin Projet")
                        .description("Test")
                        .build();

                mockMvc.perform(post("/api/v1/auth/{idTableau}", testTableau.getIdTableau())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(newProjet)))
                        .andExpect(status().isCreated());
            }
        }

    }
}