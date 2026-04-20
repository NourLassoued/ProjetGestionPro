package com.example.gestionprojeet.Service;

import com.example.gestionprojeet.Respository.MessageRepo;
import com.example.gestionprojeet.Respository.ProjetRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Message;
import com.example.gestionprojeet.classes.Projet;
import com.example.gestionprojeet.classes.Role;
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
public class MessageService {

    private final MessageRepo messageRepo;
    private final ProjetRepo projetRepo;
    private final UtlisateurRepo utilisateurRepo;

    @Transactional(readOnly = true)
    public List<Message> getMessagesByProjet(Long idProjet) {

        Projet projet = projetRepo.findById(idProjet)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        Utlisateur currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }


        if (projet.getUtilisateurs() == null || projet.getUtilisateurs().isEmpty()) {
            throw new RuntimeException("Vous n'êtes pas membre de ce projet");
        }
// ✨ Vérifier que l'utilisateur est membre du projet OU administrateur
        boolean estMembre = projet.getUtilisateurs().stream()
                .anyMatch(u -> u.getId().equals(currentUser.getId()));
        boolean estAdmin = currentUser.getRole() == Role.ADMINISTRATEUR;

        if (!estMembre && !estAdmin) {
            throw new RuntimeException("Vous n'êtes pas membre de ce projet");
        }

        return messageRepo.findByProjetIdProjetOrderByDateEnvoiAsc(idProjet);
    }
    // Envoyer un message dans un projet
    @Transactional
    public Message envoyerMessage(Long idProjet, String contenu) {
        Projet projet = projetRepo.findById(idProjet)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        Utlisateur expediteur = getCurrentUser();
        if (expediteur == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // AJOUTE LE NULL CHECK ICI AUSSI!
        if (projet.getUtilisateurs() == null || projet.getUtilisateurs().isEmpty()) {
            throw new RuntimeException("Vous n'êtes pas membre de ce projet");
        }

        // Vérifier que l'utilisateur est membre du projet OU administrateur
        boolean estMembre = projet.getUtilisateurs().stream()
                .anyMatch(u -> u.getId().equals(expediteur.getId()));
        boolean estAdmin = expediteur.getRole() == Role.ADMINISTRATEUR;

        if (!estMembre && !estAdmin) {
            throw new RuntimeException("Vous n'êtes pas membre de ce projet");
        }

        Message message = new Message();
        message.setContenu(contenu);
        message.setDateEnvoi(new Date());
        message.setExpediteur(expediteur);
        message.setProjet(projet);

        return messageRepo.save(message);
    }
    private Utlisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            return utilisateurRepo.findByEmail(email).orElse(null);
        }
        return null;
    }
}