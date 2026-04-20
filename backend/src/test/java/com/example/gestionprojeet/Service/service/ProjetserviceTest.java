package com.example.gestionprojeet.Service.service;

import com.example.gestionprojeet.Respository.*;
import com.example.gestionprojeet.Service.Projetservice;
import com.example.gestionprojeet.classes.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName(" UNIT TESTS - ProjetService")
class ProjetserviceTest {

    @Mock
    private ProjetRepo projetRepo;

    @Mock
    private TableauRepo tableauRepo;

    @Mock
    private UtlisateurRepo utilisateurRepo;

    @Mock
    private CarteRepo carteRepo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private Projetservice projetService;

    private Utlisateur admin;
    private Utlisateur member;
    private Projet projet;
    private Tableau tableau;

    @BeforeEach
    void setUp() {
        admin = Utlisateur.builder()
                .id(1L)
                .email("admin@example.com")
                .firstname("Admin")
                .role(Role.ADMINISTRATEUR)
                .build();

        member = Utlisateur.builder()
                .id(2L)
                .email("member@example.com")
                .firstname("Member")
                .role(Role.MEMBRE)
                .build();

        tableau = new Tableau();
        tableau.setIdTableau(1L);
        tableau.setNom("Test Tableau");

        projet = Projet.builder()
                .idProjet(100L)
                .nom("Original Project")
                .description("Original Description")
                .tableau(tableau)
                .build();
        projet.setUtilisateurs(new ArrayList<>());
    }

    // ============ CREATE PROJECT TESTS ============
    @Nested
    @DisplayName("CREATE PROJECT TESTS")
    class CreateProjectTests {

        @Test
        @DisplayName(" Admin peut créer un projet")
        void testAdminCanCreateProject() {
            // Setup SecurityContext
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin@example.com");
            when(authentication.isAuthenticated()).thenReturn(true);

            when(utilisateurRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
            when(tableauRepo.findById(1L)).thenReturn(Optional.of(tableau));
            when(projetRepo.save(any(Projet.class))).thenReturn(projet);

            // Execute
            Projet result = projetService.createProject(1L, projet);

            // Assert
            assertNotNull(result);
            verify(projetRepo, times(1)).save(any(Projet.class));
        }

        @Test
        @DisplayName(" Membre NE PEUT PAS créer un projet")
        void testMemberCannotCreateProject() {
            // Setup SecurityContext
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("member@example.com");
            when(authentication.isAuthenticated()).thenReturn(true);

            when(utilisateurRepo.findByEmail("member@example.com")).thenReturn(Optional.of(member));

            // Execute & Assert
            assertThrows(RuntimeException.class, () ->
                            projetService.createProject(1L, projet),
                    "Devrait lever exception: seul un administrateur"
            );

            verify(projetRepo, never()).save(any(Projet.class));
        }

        @Test
        @DisplayName(" Utilisateur non authentifié NE PEUT PAS créer")
        void testUnauthenticatedUserCannotCreateProject() {
            // Setup SecurityContext
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Execute & Assert
            assertThrows(RuntimeException.class, () ->
                            projetService.createProject(1L, projet),
                    "Utilisateur non authentifié"
            );

            verify(projetRepo, never()).save(any(Projet.class));
        }

        // ============ GET ALL PROJECTS TESTS ============
        @Nested
        @DisplayName("GET ALL PROJECTS TESTS")
        class GetAllProjectsTests {

            @Test
            @DisplayName("Récupérer tous les projets")
            void testGetAllProjects() {
                Projet projet = new Projet();
                projet.setIdProjet(1L);
                projet.setUtilisateurs(Collections.singletonList(new Utlisateur()));

                when(projetRepo.findAll()).thenReturn(Collections.singletonList(projet));

                var projets = projetService.getAllProjects();

                assertEquals(1, projets.size());
                verify(projetRepo, times(1)).findAll();
            }

            @Test
            @DisplayName(" Retourne liste vide si aucun projet")
            void testGetAllProjectsEmpty() {
                when(projetRepo.findAll()).thenReturn(Collections.emptyList());

                var projets = projetService.getAllProjects();

                assertEquals(0, projets.size());
                verify(projetRepo, times(1)).findAll();
            }
        }

        // ============ GET PROJECT BY ID TESTS ============
        @Nested
        @DisplayName("GET PROJECT BY ID TESTS")
        class GetProjectByIdTests {



            @Test
            @DisplayName("Admin peut voir un projet")
            void testAdminCanGetProject() {
                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("admin@example.com");

                when(utilisateurRepo.findByEmail("admin@example.com"))
                        .thenReturn(Optional.of(admin));
                when(projetRepo.findById(100L)).thenReturn(Optional.of(projet));

                // Execute
                Projet result = projetService.getProjetById(100L);

                // Assert
                assertNotNull(result, "Le projet ne doit pas être null");
                assertEquals("Original Project", result.getNom(),
                        "Le nom du projet doit être 'Original Project'");

                // Vérifier que les repos ont été appelés
                verify(utilisateurRepo, times(1)).findByEmail("admin@example.com");
                verify(projetRepo, times(1)).findById(100L);
            }

            @Test
            @DisplayName(" Membre du projet peut voir")
            void testMemberCanGetProject() {
                // Setup - ajouter member au projet
                if (projet.getUtilisateurs() == null) {
                    projet.setUtilisateurs(new ArrayList<>());
                }
                projet.getUtilisateurs().add(member);

                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.isAuthenticated()).thenReturn(true);

                when(authentication.getName()).thenReturn("member@example.com");

                when(utilisateurRepo.findByEmail("member@example.com")).thenReturn(Optional.of(member));
                when(projetRepo.findById(100L)).thenReturn(Optional.of(projet));

                // Execute
                Projet result = projetService.getProjetById(100L);

                // Assert
                assertNotNull(result);
            }

            @Test
            @DisplayName(" Non-membre NE PEUT PAS voir")
            void testNonMemberCannotGetProject() {
                // Setup - ne pas ajouter member au projet
                projet.setUtilisateurs(new ArrayList<>());

                Utlisateur nonMember = Utlisateur.builder()
                        .id(3L)
                        .email("nonmember@example.com")
                        .firstname("NonMember")
                        .role(Role.MEMBRE)
                        .build();

                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("nonmember@example.com");

                when(utilisateurRepo.findByEmail("nonmember@example.com")).thenReturn(Optional.of(nonMember));
                when(projetRepo.findById(100L)).thenReturn(Optional.of(projet));

                // Execute & Assert
                assertThrows(RuntimeException.class, () ->
                                projetService.getProjetById(100L),
                        "Devrait lever exception: pas membre"
                );
            }
        }

        @Nested
        @DisplayName("ADD USER TO PROJECT TESTS")
        class AddUserToProjectTests {

            @Test
            @DisplayName("Ajouter un utilisateur au projet")
            void testAjouterUtilisateurAuProjet() {
                Utlisateur user = new Utlisateur();
                user.setId(1L);
                user.setEmail("test@test.com");
                user.setProjets(new ArrayList<>());

                Projet p = new Projet();
                p.setIdProjet(1L);
                p.setUtilisateurs(new ArrayList<>());

                when(utilisateurRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(projetRepo.findById(1L)).thenReturn(Optional.of(p));
                when(projetRepo.save(any())).thenReturn(p);

                Projet result = projetService.ajouterUtilisateurAuProjetParEmail("test@test.com", 1L);

                assertTrue(result.getUtilisateurs().contains(user));
                verify(projetRepo).save(p);
            }

            @Test
            @DisplayName("Ajouter utilisateur déjà membre")
            void testAddDuplicateUser() {
                Utlisateur user = new Utlisateur();
                user.setId(1L);
                user.setEmail("test@test.com");

                Projet p = new Projet();
                p.setIdProjet(1L);
                p.setUtilisateurs(new ArrayList<>(Collections.singletonList(user)));

                when(utilisateurRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(projetRepo.findById(1L)).thenReturn(Optional.of(p));

                assertThrows(RuntimeException.class, () ->
                                projetService.ajouterUtilisateurAuProjetParEmail("test@test.com", 1L),
                        "Devrait lever exception: utilisateur déjà membre"
                );
            }
        }

        // ============ ADD CARD TO PROJECT TESTS ============
        @Nested
        @DisplayName("ADD CARD TO PROJECT TESTS")
        class AddCardToProjectTests {

            @Test
            @DisplayName("Ajouter une carte au projet")
            void testAjouterCarteAProjet() {
                Projet p = new Projet();
                p.setIdProjet(1L);

                Carte carte = new Carte();
                carte.setTitre("Test Card");

                when(projetRepo.findById(1L)).thenReturn(Optional.of(p));
                when(carteRepo.save(any(Carte.class))).thenReturn(carte);

                Carte result = projetService.ajouterCarteAProjet(1L, carte);

                assertNotNull(result);
                assertEquals("Test Card", result.getTitre());
                verify(carteRepo).save(carte);
            }
        }

        // ============ GET CARDS FROM PROJECT TESTS ============
        @Nested
        @DisplayName("GET CARDS FROM PROJECT TESTS")
        class GetCardsFromProjectTests {

            @Test
            @DisplayName(" Récupérer les cartes du projet")
            void testGetCartesDuProjet() {
                Projet p = new Projet();
                p.setIdProjet(1L);

                List<Carte> cartes = Arrays.asList(new Carte(), new Carte());

                when(projetRepo.findById(1L)).thenReturn(Optional.of(p));
                when(carteRepo.findByProjetIdProjet(1L)).thenReturn(cartes);

                List<Carte> result = projetService.getCartesDuProjet(1L);

                assertEquals(2, result.size());
                verify(carteRepo).findByProjetIdProjet(1L);
            }
        }

        // ============ UPDATE PROJECT TESTS ============
        @Nested
        @DisplayName("UPDATE PROJECT TESTS")
        class UpdateProjectTests {

            @Test
            @DisplayName("Admin peut modifier le projet")
            void testAdminCanUpdateProject() {
                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getName()).thenReturn("admin@example.com");
                when(authentication.isAuthenticated()).thenReturn(true);


                when(utilisateurRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
                when(projetRepo.findById(100L)).thenReturn(Optional.of(projet));
                when(projetRepo.save(any(Projet.class))).thenReturn(projet);

                // Execute
                Projet projetUpdate = Projet.builder()
                        .nom("Updated Project")
                        .description("Updated Description")
                        .build();

                Projet result = projetService.updateProjet(100L, projetUpdate);

                // Assert
                assertNotNull(result);
                assertEquals("Updated Project", projet.getNom());
                verify(projetRepo, times(1)).save(any(Projet.class));
            }

            @Test
            @DisplayName(" Membre NE PEUT PAS modifier")
            void testMemberCannotUpdateProject() {
                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getName()).thenReturn("member@example.com");
                when(authentication.isAuthenticated()).thenReturn(true);


                when(utilisateurRepo.findByEmail("member@example.com")).thenReturn(Optional.of(member));

                // Execute & Assert
                Projet projetUpdate = Projet.builder()
                        .nom("Hack Project")
                        .build();

                assertThrows(RuntimeException.class, () ->
                                projetService.updateProjet(100L, projetUpdate),
                        "Devrait lever exception: Accès refusé"
                );

                verify(projetRepo, never()).save(any(Projet.class));
            }

            @Test
            @DisplayName(" Utilisateur non authentifié NE PEUT PAS modifier")
            void testUnauthenticatedUserCannotUpdateProject() {
                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getName()).thenReturn("unknown@example.com");
                when(utilisateurRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
                when(authentication.isAuthenticated()).thenReturn(true);

                // Execute & Assert
                Projet projetUpdate = Projet.builder()
                        .nom("Hack Project")
                        .build();

                assertThrows(RuntimeException.class, () ->
                        projetService.updateProjet(100L, projetUpdate)
                );
            }

            @Test
            @DisplayName(" Projet non trouvé retourne exception")
            void testUpdateNonExistentProject() {
                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getName()).thenReturn("admin@example.com");
                when(authentication.isAuthenticated()).thenReturn(true);


                when(utilisateurRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
                when(projetRepo.findById(999L)).thenReturn(Optional.empty());

                // Execute & Assert
                Projet projetUpdate = Projet.builder()
                        .nom("Update")
                        .build();

                assertThrows(RuntimeException.class, () ->
                        projetService.updateProjet(999L, projetUpdate)
                );
            }

            @Test
            @DisplayName("Admin modifie juste le nom")
            void testAdminUpdateOnlyName() {
                // Setup SecurityContext
                SecurityContextHolder.setContext(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getName()).thenReturn("admin@example.com");
                when(authentication.isAuthenticated()).thenReturn(true);

                when(utilisateurRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
                when(projetRepo.findById(100L)).thenReturn(Optional.of(projet));
                when(projetRepo.save(any(Projet.class))).thenReturn(projet);

                // Execute
                Projet projetUpdate = Projet.builder()
                        .nom("New Name Only")
                        .build();

                projetService.updateProjet(100L, projetUpdate);

                // Assert
                assertEquals("New Name Only", projet.getNom());
                verify(projetRepo).save(any(Projet.class));
            }
        }
    }}