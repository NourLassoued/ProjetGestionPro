package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Respository.CarteRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/cartes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CarteControlleur {
    private final CarteRepo carteRepository;
    private final UtlisateurRepo utlisateurRepo;

    @PutMapping("/{idCarte}/statut")
    public ResponseEntity<?> updateStatut(@PathVariable Long idCarte, @RequestParam StatutCarte statut) {
        try {
            // Récupère la carte
            Carte carte = carteRepository.findById(idCarte)
                    .orElseThrow(() -> new RuntimeException("Carte non trouvée"));

            // Récupère l'utilisateur authentifié
            Utlisateur currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }

            // Récupère le projet de la carte
            Projet projet = carte.getProjet();

            // VÉRIFICATION DE PERMISSIONS
            boolean estMembre = projet.getUtilisateurs().stream()
                    .anyMatch(u -> u.getId().equals(currentUser.getId()));
            boolean estAdmin = currentUser.getRole() == Role.ADMINISTRATEUR;

            if (!estMembre && !estAdmin) {
                throw new RuntimeException("Vous n'êtes pas membre de ce projet");
            }

            // Modifie le statut
            carte.setStatut(statut);
            Carte updatedCarte = carteRepository.save(carte);
            return ResponseEntity.ok(updatedCarte);

        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";
            if (errorMessage.contains("membre") || errorMessage.contains("Vous n'êtes pas"))
                return ResponseEntity.status(403).build();  // 403 Forbidden
            if (errorMessage.contains("non trouvée"))
                return ResponseEntity.status(404).build();  // 404 Not Found
            if (errorMessage.contains("authentifié"))
                return ResponseEntity.status(401).build();  // 401 Unauthorized
            return ResponseEntity.status(500).build();
        }
    }
    private Utlisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return utlisateurRepo.findByEmail(email).orElse(null);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}
