package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.service.Projetservice;
import com.example.gestionprojeet.classes.Carte;
import com.example.gestionprojeet.classes.Projet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetControlleur {

    private final Projetservice projetService;

    // POST /api/v1/auth/create/{idTableau}
    @PostMapping("/create/{idTableau}")
    public ResponseEntity<Projet> createProject(
            @PathVariable Long idTableau,
            @RequestBody Projet projet) {
        Projet created = projetService.createProject(idTableau, projet);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET /api/v1/auth/getAllProjects
    @GetMapping("/getAllProjects")
    public ResponseEntity<List<Projet>> getAllProjects() {
        List<Projet> projects = projetService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // GET /api/v1/auth/{id} — Récupérer un projet par ID
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getProjetById(@PathVariable Long id) {
        Projet projet = projetService.getProjetById(id);
        return ResponseEntity.ok(projet);
    }

    // POST /api/v1/auth/{email}/projets/{idProjet} — Ajouter un utilisateur par email
    @PostMapping("/{email}/projets/{idProjet}")
    public ResponseEntity<Projet> ajouterUtilisateurAuProjetParEmail(
            @PathVariable String email,
            @PathVariable Long idProjet) {
        Projet projet = projetService.ajouterUtilisateurAuProjetParEmail(email, idProjet);
        return ResponseEntity.ok(projet);
    }

    // POST /api/v1/auth/{idProjet}/cartes — Ajouter une carte à un projet
    @PostMapping("/{idProjet}/cartes")
    public ResponseEntity<Carte> ajouterCarteAProjet(
            @PathVariable Long idProjet,
            @RequestBody Carte carte) {
        Carte created = projetService.ajouterCarteAProjet(idProjet, carte);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // POST /api/v1/auth/{id}/ajouterCarte/{idProjet} — Ajouter une carte pour un utilisateur
    @PostMapping("/{id}/ajouterCarte/{idProjet}")
    public ResponseEntity<Carte> ajouterCarteAuProjetPourUtilisateur(
            @PathVariable Long id,
            @PathVariable Long idProjet,
            @RequestBody Carte carte) {
        Carte created = projetService.ajouterCarteAuProjetPourUtilisateur(id, idProjet, carte);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET /api/v1/auth/{id}/projets — Récupérer les projets d'un utilisateur
    @GetMapping("/{id}/projets")
    public ResponseEntity<List<Projet>> getProjetsUtilisateur(@PathVariable Long id) {
        List<Projet> projets = projetService.getProjetsUtilisateur(id);
        return ResponseEntity.ok(projets);
    }

    // GET /api/v1/auth/{idProjet}/cartes — Récupérer les cartes d'un projet
    @GetMapping("/{idProjet}/cartes")
    public ResponseEntity<List<Carte>> getCartesDuProjet(@PathVariable Long idProjet) {
        List<Carte> cartes = projetService.getCartesDuProjet(idProjet);
        return ResponseEntity.ok(cartes);
    }
}