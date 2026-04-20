package com.example.gestionprojeet.Service.service;

import com.example.gestionprojeet.Respository.MessageRepo;
import com.example.gestionprojeet.Respository.ProjetRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.Service.MessageService;
import com.example.gestionprojeet.classes.Message;
import com.example.gestionprojeet.classes.Projet;
import com.example.gestionprojeet.classes.Utlisateur;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Test
    void testEnvoyerMessage() {
        MessageRepo messageRepo = mock(MessageRepo.class);
        ProjetRepo projetRepo = mock(ProjetRepo.class);
        UtlisateurRepo utilisateurRepo = mock(UtlisateurRepo.class);

        MessageService messageService = new MessageService(messageRepo, projetRepo, utilisateurRepo);

        Projet projet = new Projet();
        projet.setIdProjet(1L);
        Utlisateur user = new Utlisateur();
        user.setId(1L);
        projet.setUtilisateurs(Collections.singletonList(user));

        when(projetRepo.findById(1L)).thenReturn(Optional.of(projet));
        when(utilisateurRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Message savedMessage = new Message();
        savedMessage.setContenu("Bonjour");
        when(messageRepo.save(any(Message.class))).thenReturn(savedMessage);

        Message result = messageService.envoyerMessage(1L, "Bonjour");

        assertEquals("Bonjour", result.getContenu());
        verify(messageRepo, times(1)).save(any(Message.class));
    }

    @Test
    void testGetMessagesByProjet() {
        MessageRepo messageRepo = mock(MessageRepo.class);
        ProjetRepo projetRepo = mock(ProjetRepo.class);
        UtlisateurRepo utilisateurRepo = mock(UtlisateurRepo.class);

        MessageService messageService = new MessageService(messageRepo, projetRepo, utilisateurRepo);

        Projet projet = new Projet();
        projet.setIdProjet(1L);

        when(projetRepo.findById(1L)).thenReturn(Optional.of(projet));
        when(messageRepo.findByProjetIdProjetOrderByDateEnvoiAsc(1L)).thenReturn(Collections.emptyList());

        var messages = messageService.getMessagesByProjet(1L);

        assertEquals(0, messages.size());
        verify(messageRepo, times(1)).findByProjetIdProjetOrderByDateEnvoiAsc(1L);
    }
}