package com.example.gestionprojeet.SecurityTest;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.classes.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetRepo projetRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Projet projectOfUser1;
    private Projet projectOfUser2;
    private Utlisateur user1;
    private Utlisateur user2;
    private Utlisateur admin;
    @BeforeEach
    void setUp() {
        // Nettoie la BD - ORDRE CORRECT avec FK constraints
        messageRepo.deleteAll();
        messageRepo.flush();

        // Vider les relations ManyToMany
        for (Utlisateur u : utlisateurRepo.findAll()) {
            if (u.getProjets() != null) {
                u.getProjets().clear();
            }
        }
        utlisateurRepo.flush();

        // Maintenant on peut supprimer les projets
        projetRepo.deleteAll();
        projetRepo.flush();

        // Et les utilisateurs
        utlisateurRepo.deleteAll();
        utlisateurRepo.flush();

        entityManager.clear();  // ← Nettoie le cache Hibernate

        // ========== CRÉER UTILISATEUR 1 ==========
        user1 = Utlisateur.builder()
                .email("user1@example.com")
                .firstname("User 1")
                .password("password123")
                .role(Role.MEMBRE)
                .build();
        user1 = utlisateurRepo.save(user1);

        // ========== CRÉER UTILISATEUR 2 ==========
        user2 = Utlisateur.builder()
                .email("user2@example.com")
                .firstname("User 2")
                .password("password123")
                .role(Role.MEMBRE)
                .build();
        user2 = utlisateurRepo.save(user2);

        // ========== CRÉER ADMIN ==========
        admin = Utlisateur.builder()
                .email("admin@example.com")
                .firstname("Admin")
                .password("password123")
                .role(Role.ADMINISTRATEUR)
                .build();
        admin = utlisateurRepo.save(admin);

        // ========== CRÉER PROJET POUR USER 1 ==========
        projectOfUser1 = Projet.builder()
                .nom("Projet Secret de User 1")
                .description("Données confidentielles")
                .build();

        //  AJOUTER LES RELATIONS AVANT DE SAUVEGARDER!
        // Côté User: Ajouter projet à l'utilisateur
        if (user1.getProjets() == null) {
            user1.setProjets(new ArrayList<>());
        }
        user1.getProjets().add(projectOfUser1);

        // Côté Projet: Ajouter utilisateur au projet
        if (projectOfUser1.getUtilisateurs() == null) {
            projectOfUser1.setUtilisateurs(new ArrayList<>());
        }
        projectOfUser1.getUtilisateurs().add(user1);

        // MAINTENANT: Sauvegarder avec les relations synchronisées
        projectOfUser1 = projetRepo.save(projectOfUser1);
        utlisateurRepo.save(user1);

        // ========== CRÉER PROJET POUR USER 2 ==========
        projectOfUser2 = Projet.builder()
                .nom("Projet Secret de User 2")
                .description("Données de User 2")
                .build();

        // AJOUTER LES RELATIONS AVANT DE SAUVEGARDER!
        // Côté User: Ajouter projet à l'utilisateur
        if (user2.getProjets() == null) {
            user2.setProjets(new ArrayList<>());
        }
        user2.getProjets().add(projectOfUser2);

        // Côté Projet: Ajouter utilisateur au projet
        if (projectOfUser2.getUtilisateurs() == null) {
            projectOfUser2.setUtilisateurs(new ArrayList<>());
        }
        projectOfUser2.getUtilisateurs().add(user2);

        // MAINTENANT: Sauvegarder avec les relations synchronisées
        projectOfUser2 = projetRepo.save(projectOfUser2);
        utlisateurRepo.save(user2);

        // ========== AJOUTER MESSAGES ==========
        // Messages dans le projet de User 1
        Message msg1 = Message.builder()
                .contenu("Message secret de User 1")
                .projet(projectOfUser1)
                .expediteur(user1)
                .build();
        messageRepo.save(msg1);

        // Messages dans le projet de User 2
        Message msg2 = Message.builder()
                .contenu("Message secret de User 2")
                .projet(projectOfUser2)
                .expediteur(user2)
                .build();
        messageRepo.save(msg2);
    }
    // ========== TEST 1: User 1 peut voir ses propres messages ==========

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUser1CanSeeOwnMessages() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenu").value("Message secret de User 1"));
    }

    // ========== TEST 2: User 2 NE PEUT PAS voir messages de User 1 ==========

    @Test
    @WithMockUser(username = "user2@example.com", roles = {"MEMBRE"})
    void testUser2CantSeeUser1Messages() throws Exception {
        // User 2 essaye d'accéder au projet de User 1
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403 - Accès refusé!
    }

    // ========== TEST 3: User 1 NE PEUT PAS voir messages de User 2 ==========

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUser1CantSeeUser2Messages() throws Exception {
        // User 1 essaye d'accéder au projet de User 2
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser2.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403 - Accès refusé!
    }

    // ========== TEST 4: User 2 NE PEUT PAS poster dans projet de User 1 ==========

    @Test
    @WithMockUser(username = "user2@example.com", roles = {"MEMBRE"})
    void testUser2CantPostInUser1Project() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Hack attempt!");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());  // 403 - Accès refusé!
    }

    // ========== TEST 5: User 1 NE PEUT PAS poster dans projet de User 2 ==========

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUser1CantPostInUser2Project() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Hack attempt!");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", projectOfUser2.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());  // 403 - Accès refusé!
    }

    // ========== TEST 6: User 1 PEUT poster dans son propre projet ==========

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUser1CanPostInOwnProject() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Nouveau message");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());  // 201 - Succès!
    }

    // ========== TEST 7: Utilisateur non authentifié NE PEUT PAS accéder ==========

    @Test
    void testUnauthenticatedUserCantAccess() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403
    }

    // ========== TEST 8: Utilisateur non authentifié NE PEUT PAS poster ==========

    @Test
    void testUnauthenticatedUserCantPost() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Hack attempt!");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());  // 403
    }

    // ========== TEST 9: Admin PEUT voir les messages de n'importe quel projet ==========

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMINISTRATEUR"})
    void testAdminCanSeeAllProjectMessages() throws Exception {
        // Admin doit pouvoir voir les messages du projet de User 1
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenu").value("Message secret de User 1"));

        // Admin doit aussi pouvoir voir les messages du projet de User 2
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser2.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenu").value("Message secret de User 2"));
    }

    // ========== TEST 10: Admin PEUT poster dans n'importe quel projet ==========

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMINISTRATEUR"})
    void testAdminCanPostInAnyProject() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Admin message");

        // Admin peut poster dans le projet de User 1
        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());  // 201
    }

    // ========== TEST 11: Différents utilisateurs voient différents résultats ==========

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUserIsolation_User1ViewsOwnData() throws Exception {
        // User 1 voit son propre message
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenu").value("Message secret de User 1"));
    }

    @Test
    @WithMockUser(username = "user2@example.com", roles = {"MEMBRE"})
    void testUserIsolation_User2ViewsOwnData() throws Exception {
        // User 2 voit son propre message
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser2.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenu").value("Message secret de User 2"));
    }

    // ========== TEST 12: Modification non autorisée ==========

    @Test
    @WithMockUser(username = "user2@example.com", roles = {"MEMBRE"})
    void testUnauthorizedModification() throws Exception {
        // User 2 essaye de poster dans le projet de User 1
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Hack attempt!");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());  // 403
    }

    // ========== TEST 13: Token invalide ==========

    @Test
    void testInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser1.getIdProjet())
                        .header("Authorization", "Bearer invalid_token_12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403
    }

    // ========== TEST 14: Accès par ID utilisateur au lieu d'email ==========

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testSecurityByUserId() throws Exception {
        // Vérifier que User 1 ne peut pas accéder par ID utilisateur 2
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", projectOfUser2.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403
    }
}