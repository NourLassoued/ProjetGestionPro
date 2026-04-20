package com.example.gestionprojeet.Service.service;

import com.example.gestionprojeet.Respository.TableauRepo;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.Service.Tableauservice;
import com.example.gestionprojeet.classes.Tableau;
import com.example.gestionprojeet.classes.Utlisateur;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TableauserviceTest {

    @Test
    void testCreateTableau() {
        TableauRepo tableauRepo = mock(TableauRepo.class);
        UtlisateurRepo utilisateurRepo = mock(UtlisateurRepo.class);

        Tableauservice service = new Tableauservice(tableauRepo, utilisateurRepo);

        Utlisateur user = new Utlisateur();
        user.setId(1L);

        Tableau tableau = new Tableau();

        when(utilisateurRepo.findById(1L)).thenReturn(Optional.of(user));
        when(tableauRepo.save(any(Tableau.class))).thenReturn(tableau);

        Tableau result = service.createTableau(1L, tableau);

        assertNotNull(result);
        verify(tableauRepo, times(1)).save(tableau);
    }

    @Test
    void testCreateTableauByEmail() {
        TableauRepo tableauRepo = mock(TableauRepo.class);
        UtlisateurRepo utilisateurRepo = mock(UtlisateurRepo.class);

        Tableauservice service = new Tableauservice(tableauRepo, utilisateurRepo);

        Utlisateur user = new Utlisateur();
        user.setEmail("test@test.com");

        Tableau tableau = new Tableau();

        when(utilisateurRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tableauRepo.save(any(Tableau.class))).thenReturn(tableau);

        Tableau result = service.createTableauByEmail("test@test.com", tableau);

        assertNotNull(result);
        verify(tableauRepo, times(1)).save(tableau);
    }

    @Test
    void testGetTableauxByUtilisateur() {
        TableauRepo tableauRepo = mock(TableauRepo.class);
        UtlisateurRepo utilisateurRepo = mock(UtlisateurRepo.class);

        Tableauservice service = new Tableauservice(tableauRepo, utilisateurRepo);

        Utlisateur user = new Utlisateur();
        user.setId(1L);

        List<Tableau> tableaux = Arrays.asList(new Tableau(), new Tableau());
        user.setTableaux(tableaux);

        when(utilisateurRepo.findById(1L)).thenReturn(Optional.of(user));

        List<Tableau> result = service.getTableauxByUtilisateur(1L);

        assertEquals(2, result.size());
    }

    @Test
    void testGetTableauxByEmail() {
        TableauRepo tableauRepo = mock(TableauRepo.class);
        UtlisateurRepo utilisateurRepo = mock(UtlisateurRepo.class);

        Tableauservice service = new Tableauservice(tableauRepo, utilisateurRepo);

        Utlisateur user = new Utlisateur();
        user.setEmail("test@test.com");

        List<Tableau> tableaux = Arrays.asList(new Tableau(), new Tableau());
        user.setTableaux(tableaux);

        when(utilisateurRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        List<Tableau> result = service.getTableauxByEmail("test@test.com");

        assertEquals(2, result.size());
    }
}