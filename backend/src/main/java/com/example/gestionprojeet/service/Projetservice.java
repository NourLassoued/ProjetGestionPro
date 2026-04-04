package com.example.gestionprojeet.service;

import com.example.gestionprojeet.Respository.CarteRepo;
import com.example.gestionprojeet.Respository.ProjetRepo;
import com.example.gestionprojeet.Respository.TableauRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Carte;
import com.example.gestionprojeet.classes.Projet;
import com.example.gestionprojeet.classes.Tableau;
import com.example.gestionprojeet.classes.Utlisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Projetservice {

    private final ProjetRepo projetRepository;
    private final TableauRepo tableauRepository;
    private final UtlisateurRepo utilisateurRepository;
    private final CarteRepo carteRepository;

    @Transactional
    public Projet createProject(Long idTableau, Projet projet) {
        Tableau tableau = tableauRepository.findById(idTableau)
                .orElseThrow(() -> new RuntimeException("Tableau non trouvé avec l'id : " + idTableau));
        projet.setTableau(tableau);
        return projetRepository.save(projet);
    }

    @Transactional(readOnly = true)
    public List<Projet> getAllProjects() {
        List<Projet> projets = projetRepository.findAll();
        projets.forEach(p -> p.getUtilisateurs().size());
        return projets;
    }

    @Transactional(readOnly = true)
    public Projet getProjetById(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id : " + id));
        projet.getUtilisateurs().size();
        return projet;
    }

    @Transactional
    public Projet ajouterUtilisateurAuProjetParEmail(String email, Long idProjet) {
        Utlisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email : " + email));

        Projet projet = projetRepository.findById(idProjet)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id : " + idProjet));

        boolean dejaMembre = projet.getUtilisateurs().stream()
                .anyMatch(u -> u.getId().equals(utilisateur.getId()));
        if (dejaMembre) {
            throw new RuntimeException("L'utilisateur est déjà membre de ce projet.");
        }

        utilisateur.getProjets().add(projet);
        projet.getUtilisateurs().add(utilisateur);

        utilisateurRepository.save(utilisateur);
        return projetRepository.save(projet);
    }

    @Transactional
    public Carte ajouterCarteAProjet(Long idProjet, Carte carte) {
        Projet projet = projetRepository.findById(idProjet)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id : " + idProjet));

        carte.setProjet(projet);
        if (carte.getDateCreation() == null) {
            carte.setDateCreation(new Date());
        }

        Utlisateur auteur = getCurrentUser();
        if (auteur != null) {
            carte.setAuteur(auteur);
        }

        return carteRepository.save(carte);
    }

    @Transactional
    public Carte ajouterCarteAuProjetPourUtilisateur(Long userId, Long idProjet, Carte carte) {
        Utlisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        Projet projet = projetRepository.findById(idProjet)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id : " + idProjet));

        boolean estMembre = projet.getUtilisateurs().stream()
                .anyMatch(u -> u.getId().equals(utilisateur.getId()));
        if (!estMembre) {
            throw new RuntimeException("L'utilisateur n'est pas membre de ce projet.");
        }

        carte.setProjet(projet);
        carte.setAuteur(utilisateur);
        if (carte.getDateCreation() == null) {
            carte.setDateCreation(new Date());
        }
        return carteRepository.save(carte);
    }

    @Transactional(readOnly = true)
    public List<Projet> getProjetsUtilisateur(Long userId) {
        utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        List<Projet> projets = projetRepository.findByUtilisateursId(userId);
        projets.forEach(p -> p.getUtilisateurs().size());
        return projets;
    }

    @Transactional(readOnly = true)
    public List<Carte> getCartesDuProjet(Long idProjet) {
        projetRepository.findById(idProjet)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id : " + idProjet));

        return carteRepository.findByProjetIdProjet(idProjet);
    }


    private Utlisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            return utilisateurRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}