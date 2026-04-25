package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.Token.TokenRepository;
import com.example.gestionprojeet.classes.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControlleurIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetRepo projetRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private UtlisateurRepo utilisateurProjetRepo;

    @Autowired
    private TableauRepo tableauRepo;


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private  CarteRepo carteRepo;
    @Autowired
    private TokenRepository tokenRepository;
@Autowired
private  ForgetPasswordRepository forgetPasswordRepository;
    private Projet testProjet;
    private Utlisateur testUser;

    @BeforeEach
    void setup() {
        // Nuclear option: supprime TOUT
        messageRepo.deleteAll();
        forgetPasswordRepository.deleteAll();
        if (tokenRepository != null) tokenRepository.deleteAll();
        carteRepo.deleteAll();
        utilisateurProjetRepo.deleteAll();
        projetRepo.deleteAll();
        tableauRepo.deleteAll();
        utlisateurRepo.deleteAll();

        // Crée les données de test
        testUser = Utlisateur.builder()
                .email("testuser@example.com")
                .firstname("Test User")
                .password("password123")
                .role(Role.valueOf("MEMBRE"))
                .build();
        testUser = utlisateurRepo.save(testUser);

        testProjet = Projet.builder()
                .nom("Projet Test Integration")
                .description("Description du projet de test")
                .build();
        testProjet = projetRepo.save(testProjet);
        if (testUser.getProjets() == null) {
            testUser.setProjets(new ArrayList<>());
        }


        testUser.getProjets().add(testProjet);
        utlisateurRepo.save(testUser);

        Message message1 = Message.builder()
                .contenu("Premier message")
                .projet(testProjet)
                .expediteur(testUser)
                .build();
        messageRepo.save(message1);

        Message message2 = Message.builder()
                .contenu("Deuxième message")
                .projet(testProjet)
                .expediteur(testUser)
                .build();
        messageRepo.save(message2);
    }
    @Test
    void testRepositoryFindByProjet() {
        // Arrange: Les messages sont déjà créés en setup

        // Act: Récupère les messages du projet
        List<Message> messages = messageRepo.findByProjetIdProjetOrderByDateEnvoiAsc(testProjet.getIdProjet());

        // Assert
        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getContenu()).isEqualTo("Premier message");
        assertThat(messages.get(1).getContenu()).isEqualTo("Deuxième message");
    }

    @Test
    void testRepositoryFindByProjetInexistant() {
        // Act: Récupère les messages d'un projet inexistant
        List<Message> messages = messageRepo.findByProjetIdProjetOrderByDateEnvoiAsc(99999L);

        // Assert
        assertThat(messages).isEmpty();
    }

    @Test
    void testRepositorySaveMessage() {
        // Arrange
        Message newMessage = Message.builder()
                .contenu("Nouveau message")
                .projet(testProjet)
                .expediteur(testUser)
                .build();

        // Act
        Message savedMessage = messageRepo.save(newMessage);

        // Assert
        assertThat(savedMessage.getIdMessage()).isNotNull();
        assertThat(savedMessage.getContenu()).isEqualTo("Nouveau message");
        assertThat(messageRepo.count()).isEqualTo(3);
    }

    @Test
    void testRepositoryDeleteMessage() {
        // Arrange
        long initialCount = messageRepo.count();
        Message firstMessage = messageRepo.findAll().get(0);

        // Act
        messageRepo.delete(firstMessage);

        // Assert
        assertThat(messageRepo.count()).isEqualTo(initialCount - 1);
    }



    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testGetMessages_Success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testGetMessages_VerifyContent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // AJOUTER StandardCharsets.UTF_8
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(content).contains("Premier message");
        assertThat(content).contains("Deuxième message");
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testGetMessages_VerifyExpediteuer() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].expediteur.email").value("testuser@example.com"))
                .andExpect(jsonPath("$[0].expediteur.firstname").value("Test User"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testGetMessages_ProjetInexistant() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    @Test
    void testGetMessages_OrderByDateEnvoi() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())  // ✅ URL RÉELLE
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(content).contains("Deuxième message");
    }



    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_Success() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Nouveau message de test");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contenu").value("Nouveau message de test"))
                .andReturn();

        // Vérifie que le message est sauvegardé en BD
        long totalMessages = messageRepo.count();
        assertThat(totalMessages).isEqualTo(3);
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_VerifyExpediteuer() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Message avec expéditeur");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expediteur.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.expediteur.firstname").value("Test User"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_VerifyIdMessage() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Message avec ID");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMessage").exists())
                .andExpect(jsonPath("$.idMessage").isNumber());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_ContenuVide() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_ContenuNull() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", null);

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_ContenuAvecEspaces() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "   ");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_VerifyMessageCount() throws Exception {
        long initialCount = messageRepo.count();
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Test count");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        long finalCount = messageRepo.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    // ========================================
    // TESTS AUTHENTIFICATION
    // ========================================

    @Test
    void testGetMessages_SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEnvoyerMessage_SansAuthentification() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Nouveau message");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ========================================
    // TESTS FLUX COMPLET
    // ========================================

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testFluxComplet_CreerEtRecupererMessages() throws Exception {
        // 1. Vérifie les messages initiaux
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // 2. Crée un nouveau message
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Troisième message");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // 3. Vérifie qu'il y a maintenant 3 messages
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testFluxComplet_CreerMultipleMessagesEtVerifier() throws Exception {
        // Crée 5 messages
        for (int i = 1; i <= 5; i++) {
            Map<String, String> body = new HashMap<>();
            body.put("contenu", "Message " + i);

            mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());
        }

        // Vérifie qu'il y a 7 messages (2 initiaux + 5 nouveaux)
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));

        // Vérifie le contenu du dernier message
        MvcResult result = mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("Message 1", "Message 5");
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testRepositoryAndControllerSync() throws Exception {
        // Crée un message via controller
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Message sync test");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Vérifie via repository
        List<Message> messages = messageRepo.findByProjetIdProjetOrderByDateEnvoiAsc(testProjet.getIdProjet());
        assertThat(messages).hasSize(3);
        assertThat(messages).anyMatch(m -> m.getContenu().equals("Message sync test"));

        // Vérifie via controller
        mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ========================================
    // TESTS VALIDATION
    // ========================================

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_ContenuAvecCaracteresSpeciaux() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("contenu", "Message avec !@#$%^&*() caractères spéciaux");

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contenu").value("Message avec !@#$%^&*() caractères spéciaux"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testEnvoyerMessage_ContenuLong() throws Exception {
        Map<String, String> body = new HashMap<>();
        String longContent = "A".repeat(1000);
        body.put("contenu", longContent);

        mockMvc.perform(post("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testGetMessages_ResponseFormat() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/messages/projet/{idProjet}", testProjet.getIdProjet())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMessage").exists())
                .andExpect(jsonPath("$[0].contenu").exists())
                .andExpect(jsonPath("$[0].expediteur").exists())
                .andReturn();

        assertThat(result.getResponse().getContentType()).contains("application/json");
    }
}