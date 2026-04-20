package com.example.gestionprojeet.SecurityTest;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.classes.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;


import static org.hamcrest.Matchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@DisplayName(" SECURITY TESTS - Projet (Permissions)")
class ProjetSecurityTest {

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


    private Utlisateur creator;
    private Utlisateur member;
    private Utlisateur nonMember;
    private Utlisateur admin;

    private Tableau creatorTableau;
    private Projet creatorProjet;

    @BeforeEach
    void setup() {
        carteRepo.deleteAll();
        projetRepo.deleteAll();
        tableauRepo.deleteAll();
        utlisateurRepo.deleteAll();

        creator = Utlisateur.builder()
                .firstname("John Creator")
                .email("creator@example.com")
                .password("password123")
                .role(Role.MEMBRE)
                .build();
        creator = utlisateurRepo.save(creator);

        member = Utlisateur.builder()
                .firstname("Jane Member")
                .email("member@example.com")
                .password("password123")
                .role(Role.MEMBRE)
                .build();
        member = utlisateurRepo.save(member);

        nonMember = Utlisateur.builder()
                .firstname("Bob NonMember")
                .email("nonmember@example.com")
                .password("password123")
                .role(Role.MEMBRE)
                .build();
        nonMember = utlisateurRepo.save(nonMember);

        admin = Utlisateur.builder()
                .firstname("Admin User")
                .email("admin@example.com")
                .password("password123")
                .role(Role.ADMINISTRATEUR)
                .build();
        admin = utlisateurRepo.save(admin);

        creatorTableau = Tableau.builder()
                .nom("Creator Tableau")
                .description("Tableau du créateur")
                .proprietaire(creator)
                .build();
        creatorTableau = tableauRepo.save(creatorTableau);

        creatorProjet = Projet.builder()
                .nom("Creator Project")
                .description("Projet créé par John")
                .tableau(creatorTableau)
                .build();
        creatorProjet = projetRepo.save(creatorProjet);

        if (creatorProjet.getUtilisateurs() == null) {
            creatorProjet.setUtilisateurs(new ArrayList<>());
        }
        creatorProjet.getUtilisateurs().add(member);
        projetRepo.save(creatorProjet);
    }

    @Nested
    @DisplayName("VIEW PROJECT - Qui peut voir?")
    class ViewProjectTests {

        @Test
        @DisplayName("Admin peut créer un projet")
        @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
        void testAdminCanCreateProject() throws Exception {
            Projet newProjet = Projet.builder()
                    .nom("New Admin Project")
                    .description("Créé par admin")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{idTableau}",
                                    creatorTableau.getIdTableau())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newProjet)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Membre NE PEUT PAS créer un projet")
        @WithMockUser(username = "member@example.com", roles = "MEMBRE")
        void testMemberCannotCreateProject() throws Exception {
            Projet newProjet = Projet.builder()
                    .nom("Hacker Project")
                    .description("Essai de hacker")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{idTableau}",
                                    creatorTableau.getIdTableau())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(newProjet)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName(" Membre peut voir le projet")
        @WithMockUser(username = "member@example.com", roles = "MEMBRE")
        void testMemberCanViewProject() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                            creatorProjet.getIdProjet().intValue()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom", equalTo("Creator Project")));
        }

        @Test
        @DisplayName(" Non-membre NE PEUT PAS voir le projet")
        @WithMockUser(username = "nonmember@example.com", roles = "MEMBRE")
        void testNonMemberCannotViewProject() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                            creatorProjet.getIdProjet().intValue()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin peut voir TOUS les projets")
        @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
        void testAdminCanViewAllProjects() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                            creatorProjet.getIdProjet().intValue()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom", equalTo("Creator Project")));
        }
    }

    @Test
    @DisplayName("Admin modifie un projet - AUTORISÉ 200")
    @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
    void testAdminCanModifyProject() throws Exception {
        Projet updateProjet = Projet.builder()
                .nom("Modifié par Admin")
                .description("Admin a changé ça")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/{id}",
                                creatorProjet.getIdProjet())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", equalTo("Modifié par Admin")));
    }

    @Test
    @DisplayName("Member modifie un projet - INTERDIT 403")
    @WithMockUser(username = "member@example.com", roles = "MEMBRE")
    void testMemberCannotModifyProject() throws Exception {
        Projet updateProjet = Projet.builder()
                .nom("Hack")
                .description("Tentative")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/{id}",
                                creatorProjet.getIdProjet())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Non-authentifié modifie - INTERDIT 403")
    void testUnauthenticatedCannotModifyProject() throws Exception {
        Projet updateProjet = Projet.builder()
                .nom("Hack")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/{id}",
                                creatorProjet.getIdProjet())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Créateur (member) modifie son projet - INTERDIT 403")
    @WithMockUser(username = "creator@example.com", roles = "MEMBRE")
    void testCreatorCannotModifyOwnProject() throws Exception {
        Projet updateProjet = Projet.builder()
                .nom("Modifié par créateur")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/{id}",
                                creatorProjet.getIdProjet())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isForbidden());
    }

    @Nested
    @DisplayName("DELETE PROJECT - Qui peut supprimer?")
    class DeleteProjectSecurityTests {

        @Test
        @DisplayName("Admin supprime un projet - AUTORISÉ 200")
        @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
        void testAdminCanDeleteProject() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/auth/{id}",
                            creatorProjet.getIdProjet()))
                    .andExpect(status().isOk());

            // Vérifier suppression
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                            creatorProjet.getIdProjet()))
                    .andExpect(status().isNotFound());
        }

    }

    @Test
    @DisplayName("Member supprime un projet - INTERDIT 403")
    @WithMockUser(username = "member@example.com", roles = "MEMBRE")
    void testMemberCannotDeleteProject() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/auth/{id}",
                        creatorProjet.getIdProjet()))
                .andExpect(status().isForbidden());

        // Vérifier qu'il n'est pas supprimé
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                        creatorProjet.getIdProjet()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Non-authentifié supprime - INTERDIT 403")
    void testUnauthenticatedCannotDeleteProject() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/auth/{id}",
                        creatorProjet.getIdProjet()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Créateur supprime son projet - INTERDIT 403")
    @WithMockUser(username = "creator@example.com", roles = "MEMBRE")
    void testCreatorCannotDeleteOwnProject() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/auth/{id}",
                        creatorProjet.getIdProjet()))
                .andExpect(status().isForbidden());
    }

    @Nested
    @DisplayName("ADD USER - Qui peut ajouter utilisateur?")
    class AddUserSecurityTests {

        @Test
        @DisplayName("Admin ajoute utilisateur - AUTORISÉ 200")
        @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
        void testAdminCanAddUserToProject() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{email}/projets/{idProjet}",
                            "nonmember@example.com", creatorProjet.getIdProjet()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Member ajoute utilisateur - INTERDIT 403")
        @WithMockUser(username = "member@example.com", roles = "MEMBRE")
        void testMemberCannotAddUserToProject() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{email}/projets/{idProjet}",
                            "nonmember@example.com", creatorProjet.getIdProjet()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Non-authentifié ajoute - INTERDIT 403")
        void testUnauthenticatedCannotAddUser() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{email}/projets/{idProjet}",
                            "nonmember@example.com", creatorProjet.getIdProjet()))
                    .andExpect(status().isForbidden());
        }


    }

    @Nested
    @DisplayName("ADD CARD - Qui peut ajouter une carte?")
    class AddCardSecurityTests {

        @Test
        @DisplayName("Member ajoute carte au projet - AUTORISÉ 201")
        @WithMockUser(username = "member@example.com", roles = "MEMBRE")
        void testMemberCanAddCardToProject() throws Exception {
            Carte carte = Carte.builder()
                    .titre("Nouvelle Carte")
                    .description("Test")
                    .dateCreation(new Date())
                    .statut(StatutCarte.A_FAIRE)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{idProjet}/cartes",
                                    creatorProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(carte)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Admin ajoute carte - AUTORISÉ 201")
        @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
        void testAdminCanAddCardToProject() throws Exception {
            Carte carte = Carte.builder()
                    .titre("Carte Admin")
                    .description("Test")
                    .dateCreation(new Date())
                    .statut(StatutCarte.A_FAIRE)
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{idProjet}/cartes",
                                    creatorProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(carte)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Non-authentifié ajoute carte - INTERDIT 403")
        void testUnauthenticatedCannotAddCard() throws Exception {
            Carte carte = Carte.builder()
                    .titre("Hack")
                    .dateCreation(new Date())
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{idProjet}/cartes",
                                    creatorProjet.getIdProjet())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(carte)))
                    .andExpect(status().isForbidden());
        }


    }

    @Nested
    @DisplayName("DATA VALIDATION SECURITY")
    class DataValidationSecurityTests {


        @Nested
        @DisplayName("PRIVILEGE ESCALATION TESTS")
        class PrivilegeEscalationTests {

            @Test
            @DisplayName("Member essaie d'accéder comme Admin - BLOCKÉ")
            @WithMockUser(username = "member@example.com", roles = "MEMBRE")
            void testMemberCannotAccessAdminEndpoints() throws Exception {
                // getAllProjects est admin-only
                mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/getAllProjects"))
                        .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("Member essaie de modifier l'ID dans l'URL - INTERDIT")
            @WithMockUser(username = "member@example.com", roles = "MEMBRE")
            void testMemberCannotBypassIdCheck() throws Exception {
                Projet updateProjet = Projet.builder()
                        .nom("Hack")
                        .build();

                mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/{id}",
                                        creatorProjet.getIdProjet())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(updateProjet)))
                        .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("Admin ne peut pas utiliser les rôles de Member")
            @WithMockUser(username = "admin@example.com", roles = "ADMINISTRATEUR")
            void testAdminCannotLowerPrivileges() throws Exception {
                // Admin peut faire ce qu'un member peut faire
                Carte carte = Carte.builder()
                        .titre("Admin Carte")
                        .dateCreation(new Date())
                        .statut(StatutCarte.A_FAIRE)
                        .build();

                mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/{idProjet}/cartes",
                                        creatorProjet.getIdProjet())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(carte)))
                        .andExpect(status().isCreated());
            }
        }

        @Nested
        @DisplayName("CROSS-USER ACCESS TESTS")
        class CrossUserAccessTests {

            @Test
            @DisplayName("Member A voir le projet de Member B (si membre) - OK")
            @WithMockUser(username = "member@example.com", roles = "MEMBRE")
            void testMemberACanSeeMemberBProjectIfMember() throws Exception {
                // member est dans creatorProjet
                mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                                creatorProjet.getIdProjet()))
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("Member A NE PEUT PAS voir projet de Creator (si pas membre)")
            void testMemberCannotSeeProjectIfNotMember() throws Exception {
                // Créer un nouveau projet où nonmember n'est pas ajouté
                Tableau tableau2 = Tableau.builder()
                        .nom("Tableau 2")
                        .proprietaire(creator)
                        .build();
                tableau2 = tableauRepo.save(tableau2);

                Projet projetFermé = Projet.builder()
                        .nom("Projet Fermé")
                        .tableau(tableau2)
                        .build();
                projetFermé = projetRepo.save(projetFermé);

                mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/{id}",
                                        projetFermé.getIdProjet())
                                .with(SecurityMockMvcRequestPostProcessors.httpBasic("nonmember@example.com", "password123")))
                        .andExpect(status().isForbidden());
            }
        }



        }

    }






