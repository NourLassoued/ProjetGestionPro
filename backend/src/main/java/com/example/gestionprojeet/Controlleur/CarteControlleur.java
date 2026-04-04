package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Respository.CarteRepo;
import com.example.gestionprojeet.classes.Carte;
import com.example.gestionprojeet.classes.StatutCarte;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/cartes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CarteControlleur {
    private final CarteRepo carteRepository;

    @PutMapping("/{idCarte}/statut")
    public ResponseEntity<Carte> updateStatut(
            @PathVariable Long idCarte,
            @RequestParam StatutCarte statut) {
        Carte carte = carteRepository.findById(idCarte)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée avec l'id : " + idCarte));
        carte.setStatut(statut);
        return ResponseEntity.ok(carteRepository.save(carte));
    }
}
