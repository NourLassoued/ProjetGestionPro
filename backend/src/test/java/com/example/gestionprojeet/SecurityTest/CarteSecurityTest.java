package com.example.gestionprojeet.SecurityTest;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
@ActiveProfiles("test")
class CarteSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CarteRepo carteRepo;
    @Autowired private ProjetRepo projetRepo;
    @Autowired private UtlisateurRepo utlisateurRepo;
    @Autowired private TableauRepo tableauRepo;

    private Projet projectOfUser1, projectOfUser2;
    private Utlisateur user1, user2, admin;
    private Carte carteUser1, carteUser2;

    @BeforeEach
    void setUp() {
        carteRepo.deleteAll();
        carteRepo.flush();
        for (Utlisateur u : utlisateurRepo.findAll()) {
            if (u.getProjets() != null) u.getProjets().clear();
        }
        utlisateurRepo.flush();
        projetRepo.deleteAll();
        projetRepo.flush();
        tableauRepo.deleteAll();
        tableauRepo.flush();
        utlisateurRepo.deleteAll();
        utlisateurRepo.flush();

        user1 = Utlisateur.builder().email("user1@example.com").firstname("User 1").password("password123").role(Role.MEMBRE).build();
        user1 = utlisateurRepo.save(user1);

        user2 = Utlisateur.builder().email("user2@example.com").firstname("User 2").password("password123").role(Role.MEMBRE).build();
        user2 = utlisateurRepo.save(user2);

        admin = Utlisateur.builder().email("admin@example.com").firstname("Admin").password("password123").role(Role.ADMINISTRATEUR).build();
        admin = utlisateurRepo.save(admin);

        projectOfUser1 = Projet.builder().nom("Projet de User 1").description("Données confidentielles User 1").build();
        if (user1.getProjets() == null) user1.setProjets(new ArrayList<>());
        user1.getProjets().add(projectOfUser1);
        if (projectOfUser1.getUtilisateurs() == null) projectOfUser1.setUtilisateurs(new ArrayList<>());
        projectOfUser1.getUtilisateurs().add(user1);
        projectOfUser1 = projetRepo.save(projectOfUser1);
        utlisateurRepo.save(user1);

        projectOfUser2 = Projet.builder().nom("Projet de User 2").description("Données confidentielles User 2").build();
        if (user2.getProjets() == null) user2.setProjets(new ArrayList<>());
        user2.getProjets().add(projectOfUser2);
        if (projectOfUser2.getUtilisateurs() == null) projectOfUser2.setUtilisateurs(new ArrayList<>());
        projectOfUser2.getUtilisateurs().add(user2);
        projectOfUser2 = projetRepo.save(projectOfUser2);
        utlisateurRepo.save(user2);

        carteUser1 = Carte.builder().titre("Carte secrète User 1").description("Données confidentielles de User 1").statut(StatutCarte.A_FAIRE).projet(projectOfUser1).auteur(user1).dateCreation(new Date()).build();
        carteUser1 = carteRepo.save(carteUser1);

        carteUser2 = Carte.builder().titre("Carte secrète User 2").description("Données confidentielles de User 2").statut(StatutCarte.EN_COURS).projet(projectOfUser2).auteur(user2).dateCreation(new Date()).build();
        carteUser2 = carteRepo.save(carteUser2);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUser1CanUpdateOwnCarte() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", carteUser1.getIdCarte()).param("statut", "TERMINE").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("TERMINE"));
    }

    @Test
    @WithMockUser(username = "user2@example.com", roles = {"MEMBRE"})
    void testUser2CantUpdateUser1Carte() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", carteUser1.getIdCarte()).param("statut", "TERMINE").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"MEMBRE"})
    void testUser1CantUpdateUser2Carte() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", carteUser2.getIdCarte()).param("statut", "TERMINE").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMINISTRATEUR"})
    void testAdminCanUpdateAnyCarte() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", carteUser1.getIdCarte()).param("statut", "TERMINE").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("TERMINE"));
    }

    @Test
    void testUnauthenticatedCantUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", carteUser1.getIdCarte()).param("statut", "TERMINE").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void testInvalidTokenCantUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cartes/{idCarte}/statut", carteUser1.getIdCarte()).header("Authorization", "Bearer invalid_token").param("statut", "TERMINE").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }
}