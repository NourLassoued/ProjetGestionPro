package com.example.gestionprojeet.Service;

import com.example.gestionprojeet.Respository.TableauRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Tableau;
import com.example.gestionprojeet.classes.Utlisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Tableauservice {
    private final TableauRepo tableauRepository;
    private final UtlisateurRepo utilisateurRepository;


    @Transactional
    public Tableau createTableau(Long userId, Tableau tableau) {
        Utlisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));
        tableau.setProprietaire(utilisateur);
        return tableauRepository.save(tableau);
    }

    @Transactional
    public Tableau createTableauByEmail(String email, Tableau tableau) {
        Utlisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email : " + email));
        tableau.setProprietaire(utilisateur);
        return tableauRepository.save(tableau);
    }

    @Transactional(readOnly = true)
    public List<Tableau> getTableauxByUtilisateur(Long userId) {
        Utlisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));
        List<Tableau> tableaux = utilisateur.getTableaux();
        return tableaux != null ? tableaux : new ArrayList<>();
    }

    @Transactional(readOnly = true)
    public List<Tableau> getTableauxByEmail(String email) {
        Utlisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email : " + email));
        List<Tableau> tableaux = utilisateur.getTableaux();
        return tableaux != null ? tableaux : new ArrayList<>();
    }
}


