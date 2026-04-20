package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Service.Projetservice;
import com.example.gestionprojeet.classes.Carte;
import com.example.gestionprojeet.classes.Projet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetControlleur {

    private final Projetservice projetService;

    @PostMapping("/{idTableau}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> createProject(@PathVariable Long idTableau, @RequestBody Projet projet) {
        try {
            Projet createdProjet = projetService.createProject(idTableau, projet);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProjet);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Accès refusé")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/getAllProjects")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<List<Projet>> getAllProjects() {
        List<Projet> projects = projetService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjetById(@PathVariable Long id) {
        try {
            Projet projet = projetService.getProjetById(id);
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Accès refusé")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{email}/projets/{idProjet}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Projet> ajouterUtilisateurAuProjetParEmail(
            @PathVariable String email,
            @PathVariable Long idProjet) {
        try {
            Projet projet = projetService.ajouterUtilisateurAuProjetParEmail(email, idProjet);
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("non trouvé")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("déjà ajouté")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{idProjet}/cartes")
    public ResponseEntity<Carte> ajouterCarteAProjet(
            @PathVariable Long idProjet,
            @RequestBody Carte carte) {
        Carte created = projetService.ajouterCarteAProjet(idProjet, carte);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/ajouterCarte/{idProjet}")
    public ResponseEntity<Carte> ajouterCarteAuProjetPourUtilisateur(
            @PathVariable Long id,
            @PathVariable Long idProjet,
            @RequestBody Carte carte) {
        Carte created = projetService.ajouterCarteAuProjetPourUtilisateur(id, idProjet, carte);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/projets")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<List<Projet>> getProjetsUtilisateur(@PathVariable Long id) {
        List<Projet> projets = projetService.getProjetsUtilisateur(id);
        return ResponseEntity.ok(projets);
    }

    @GetMapping("/{idProjet}/cartes")
    public ResponseEntity<List<Carte>> getCartesDuProjet(@PathVariable Long idProjet) {
        List<Carte> cartes = projetService.getCartesDuProjet(idProjet);
        return ResponseEntity.ok(cartes);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> updateProjet(@PathVariable Long id, @RequestBody Projet projetDetails) {
        try {
            Projet updatedProjet = projetService.updateProjet(id, projetDetails);
            return ResponseEntity.ok(updatedProjet);
        } catch (RuntimeException e) {
            // Si "Accès refusé" → 403
            if (e.getMessage().contains("Accès refusé")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Sinon → 404
            return ResponseEntity.notFound().build();
        }

    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.ok().build();  // 200 OK
        } catch (RuntimeException e) {
            // Si pas admin → 403 Forbidden
            if (e.getMessage().contains("Accès refusé")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Si projet inexistant → 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

}