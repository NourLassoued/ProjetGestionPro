package com.example.gestionprojeet.integration;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.classes.*;
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

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CarteRepositoryAndControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarteRepo carteRepo;

    @Autowired
    private ProjetRepo projetRepo;

    @Autowired
    private UtlisateurRepo utlisateurRepo;

    @Autowired
    private UtlisateurRepo utilisateurProjetRepo;

    @Autowired
    private TableauRepo tableauRepo;

    private Projet testProjet;
    private Utlisateur testUser;
    private Carte testCarte1;
    private Carte testCarte2;

    @BeforeEach
    void setup() {
        // ✅ Simple cleanup
        carteRepo.deleteAll();
        utilisateurProjetRepo.deleteAll();
        projetRepo.deleteAll();
        tableauRepo.deleteAll();
        utlisateurRepo.deleteAll();

        // Crée un utilisateur de test
        testUser = Utlisateur.builder()
                .email("testuser@example.com")
                .firstname("Test User")
                .password("password123")
                .role(Role.valueOf("MEMBRE"))
                .build();
        testUser = utlisateurRepo.save(testUser);

        // Crée un projet de test
        testProjet = Projet.builder()
                .nom("Projet Test")
                .description("Description du projet")
                .build();
        testProjet = projetRepo.save(testProjet);

        // Crée 2 cartes pour le même projet
        testCarte1 = Carte.builder()
                .titre("Carte 1")
                .description("Description 1")
                .statut(StatutCarte.A_FAIRE)
                .projet(testProjet)
                .auteur(testUser)
                .dateCreation(new Date())
                .build();
        testCarte1 = carteRepo.save(testCarte1);

        testCarte2 = Carte.builder()
                .titre("Carte 2")
                .description("Description 2")
                .statut(StatutCarte.EN_COURS)
                .projet(testProjet)
                .auteur(testUser)
                .dateCreation(new Date())
                .build();
        testCarte2 = carteRepo.save(testCarte2);
    }

    // ========================================
    // TESTS REPOSITORY - findByProjetIdProjet
    // ========================================

    @Test
    void testRepositoryFindByProjet() {
        // Act
        List<Carte> cartes = carteRepo.findByProjetIdProjet(testProjet.getIdProjet());

        // Assert
        assertThat(cartes).isNotEmpty();
        assertThat(cartes).hasSize(2);
        assertThat(cartes.stream().map(Carte::getTitre).toList())
                .contains("Carte 1", "Carte 2");
    }

    @Test
    void testRepositoryFindByProjet_VerifyContent() {
        // Act
        List<Carte> cartes = carteRepo.findByProjetIdProjet(testProjet.getIdProjet());

        // Assert
        assertThat(cartes.get(0).getTitre()).isEqualTo("Carte 1");
        assertThat(cartes.get(0).getStatut()).isEqualTo(StatutCarte.A_FAIRE);
        assertThat(cartes.get(1).getTitre()).isEqualTo("Carte 2");
        assertThat(cartes.get(1).getStatut()).isEqualTo(StatutCarte.EN_COURS);
    }

    @Test
    void testRepositoryFindByProjet_ProjetInexistant() {
        // Act
        List<Carte> cartes = carteRepo.findByProjetIdProjet(99999L);

        // Assert
        assertThat(cartes).isEmpty();
    }

    @Test
    void testRepositoryFindById() {
        // Act
        Carte foundCarte = carteRepo.findById(testCarte1.getIdCarte()).orElse(null);

        // Assert
        assertThat(foundCarte).isNotNull();
        assertThat(foundCarte.getTitre()).isEqualTo("Carte 1");
    }

    @Test
    void testRepositorySaveCarte() {
        // Arrange
        long initialCount = carteRepo.count();
        Carte newCarte = Carte.builder()
                .titre("Nouvelle Carte")
                .description("Description")
                .statut(StatutCarte.A_FAIRE)
                .projet(testProjet)
                .auteur(testUser)
                .dateCreation(new Date())
                .build();

        // Act
        Carte savedCarte = carteRepo.save(newCarte);

        // Assert
        assertThat(savedCarte.getIdCarte()).isNotNull();
        assertThat(carteRepo.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void testRepositoryUpdateStatut() {
        // Arrange
        testCarte1.setStatut(StatutCarte.TERMINE);

        // Act
        carteRepo.save(testCarte1);

        // Assert
        Carte updatedCarte = carteRepo.findById(testCarte1.getIdCarte()).orElse(null);
        assertThat(updatedCarte.getStatut()).isEqualTo(StatutCarte.TERMINE);
    }

    @Test
    void testRepositoryDeleteCarte() {
        // Arrange
        long initialCount = carteRepo.count();

        // Act
        carteRepo.delete(testCarte1);

        // Assert
        assertThat(carteRepo.count()).isEqualTo(initialCount - 1);
        assertThat(carteRepo.findById(testCarte1.getIdCarte())).isEmpty();
    }

    // ========================================
    // TESTS CONTROLLER - PUT /{idCarte}/statut
    // ========================================

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_Success() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte1.getIdCarte())
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINE"))
                .andExpect(jsonPath("$.titre").value("Carte 1"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_ToEnCours() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte1.getIdCarte())
                        .param("statut", "EN_COURS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_ToAFaire() throws Exception {
        // Change d'abord à TERMINE
        testCarte2.setStatut(StatutCarte.TERMINE);
        carteRepo.save(testCarte2);

        // Ensuite revenir à A_FAIRE
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte2.getIdCarte())
                        .param("statut", "A_FAIRE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("A_FAIRE"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_VerifyPersisted() throws Exception {
        // Appel l'endpoint
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte1.getIdCarte())
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Vérifie en BD
        Carte updatedCarte = carteRepo.findById(testCarte1.getIdCarte()).orElse(null);
        assertThat(updatedCarte.getStatut()).isEqualTo(StatutCarte.TERMINE);
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_CarteNonTrouvee() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", 99999L)
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_InvalidEnum() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte1.getIdCarte())
                        .param("statut", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void testUpdateStatut_SansAuthentification() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte1.getIdCarte())
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========================================
    // TESTS INTEGRATION REPO + CONTROLLER
    // ========================================

    @Test
    void testRepositorySyncWithDatabase() {
        // Arrange
        long initialCount = carteRepo.count();
        Carte newCarte = Carte.builder()
                .titre("Sync Test")
                .description("Test sync")
                .statut(StatutCarte.A_FAIRE)
                .projet(testProjet)
                .auteur(testUser)
                .dateCreation(new Date())
                .build();

        // Act
        carteRepo.save(newCarte);

        // Assert
        List<Carte> cartes = carteRepo.findByProjetIdProjet(testProjet.getIdProjet());
        assertThat(cartes).hasSize((int)(initialCount + 1));
        assertThat(cartes.stream().map(Carte::getTitre).toList()).contains("Sync Test");
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testControllerAndRepositorySync() throws Exception {
        // 1. Crée une carte via repository
        Carte newCarte = Carte.builder()
                .titre("Controller Repo Sync")
                .description("Test")
                .statut(StatutCarte.A_FAIRE)
                .projet(testProjet)
                .auteur(testUser)
                .dateCreation(new Date())
                .build();
        Carte savedCarte = carteRepo.save(newCarte);

        // 2. Met à jour via controller
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", savedCarte.getIdCarte())
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 3. Vérifie via repository
        Carte updatedCarte = carteRepo.findById(savedCarte.getIdCarte()).orElse(null);
        assertThat(updatedCarte.getStatut()).isEqualTo(StatutCarte.TERMINE);
    }

    @Test
    void testMultipleCartesSameProjet() {
        // Arrange: 2 cartes déjà créées

        // Act
        List<Carte> cartes = carteRepo.findByProjetIdProjet(testProjet.getIdProjet());

        // Assert
        assertThat(cartes).hasSize(2);
        assertThat(cartes.get(0).getProjet().getIdProjet()).isEqualTo(testProjet.getIdProjet());
        assertThat(cartes.get(1).getProjet().getIdProjet()).isEqualTo(testProjet.getIdProjet());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testFluxComplet_CreerEtMettreAJour() throws Exception {
        // 1. Crée une carte
        Carte newCarte = Carte.builder()
                .titre("Flux Complet")
                .description("Test flux")
                .statut(StatutCarte.A_FAIRE)
                .projet(testProjet)
                .auteur(testUser)
                .dateCreation(new Date())
                .build();
        Carte savedCarte = carteRepo.save(newCarte);

        // 2. Met à jour le statut à EN_COURS
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", savedCarte.getIdCarte())
                        .param("statut", "EN_COURS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));

        // 3. Met à jour le statut à TERMINE
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", savedCarte.getIdCarte())
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINE"));

        // 4. Vérifie en repository
        Carte finalCarte = carteRepo.findById(savedCarte.getIdCarte()).orElse(null);
        assertThat(finalCarte.getStatut()).isEqualTo(StatutCarte.TERMINE);
    }

    @Test
    void testRepositoryCount() {
        // Act
        long count = carteRepo.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testRepositoryCountByProjet() {
        // Act
        List<Carte> cartes = carteRepo.findByProjetIdProjet(testProjet.getIdProjet());

        // Assert
        assertThat(cartes).hasSize(2);
        assertThat(cartes.stream().filter(c -> c.getStatut() == StatutCarte.A_FAIRE).count()).isEqualTo(1);
        assertThat(cartes.stream().filter(c -> c.getStatut() == StatutCarte.EN_COURS).count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"MEMBRE"})
    void testUpdateStatut_ResponseContainsAllFields() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", testCarte1.getIdCarte())
                        .param("statut", "TERMINE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarte").exists())
                .andExpect(jsonPath("$.titre").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.statut").exists())
                .andExpect(jsonPath("$.dateCreation").exists())
                .andExpect(jsonPath("$.auteur").exists());
    }

    @Test
    void testRepositoryAuteurRelation() {
        // Act
        Carte foundCarte = carteRepo.findById(testCarte1.getIdCarte()).orElse(null);

        // Assert
        assertThat(foundCarte).isNotNull();
        assertThat(foundCarte.getAuteur()).isNotNull();
        assertThat(foundCarte.getAuteur().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    void testRepositoryProjetRelation() {
        // Act
        Carte foundCarte = carteRepo.findById(testCarte1.getIdCarte()).orElse(null);

        // Assert
        assertThat(foundCarte).isNotNull();
        assertThat(foundCarte.getProjet()).isNotNull();
        assertThat(foundCarte.getProjet().getIdProjet()).isEqualTo(testProjet.getIdProjet());
    }
}