package com.example.gestionprojeet.Service.service;

import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.Service.Utlisateurservice;
import com.example.gestionprojeet.classes.Utlisateur;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtlisateurserviceTest {

    @Test
    void testSaveUser() {
        UtlisateurRepo userRepository = mock(UtlisateurRepo.class);


        Utlisateurservice service = new Utlisateurservice();
        service.userRepository = userRepository;

        Utlisateur user = new Utlisateur();
        user.setId(1L);

        service.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetAllUser() {
        UtlisateurRepo userRepository = mock(UtlisateurRepo.class);

        Utlisateurservice service = new Utlisateurservice();
        service.userRepository = userRepository;


        List<Utlisateur> users = Arrays.asList(new Utlisateur(), new Utlisateur());

        when(userRepository.findAll()).thenReturn(users);

        List<Utlisateur> result = service.getAllUser();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testDeleteUser() {
        UtlisateurRepo userRepository = mock(UtlisateurRepo.class);

        Utlisateurservice service = new Utlisateurservice();
        service.userRepository = userRepository;

        service.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateUser() {
        UtlisateurRepo userRepository = mock(UtlisateurRepo.class);

        Utlisateurservice service = new Utlisateurservice();
        service.userRepository = userRepository;


        Utlisateur existingUser = new Utlisateur();
        existingUser.setId(1L);
        existingUser.setFirstname("Old");
        existingUser.setEmail("old@test.com");
        existingUser.setPassword("123");


        Utlisateur updatedInput = new Utlisateur();
        updatedInput.setId(1L);
        updatedInput.setFirstname("New");
        updatedInput.setEmail("new@test.com");
        updatedInput.setPassword("456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(Utlisateur.class))).thenReturn(existingUser);

        Utlisateur result = service.updateUser(updatedInput);

        assertEquals("New", result.getFirstname());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("456", result.getPassword());

        verify(userRepository, times(1)).save(existingUser);
    }
}