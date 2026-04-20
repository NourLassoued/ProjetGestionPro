package com.example.gestionprojeet.Service.service;

import com.example.gestionprojeet.Config.JwtService;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.Token.TokenRepository;
import com.example.gestionprojeet.auth.AuthenticationReponse;
import com.example.gestionprojeet.auth.AuthenticationService;
import com.example.gestionprojeet.auth.RegisterRequest;
import com.example.gestionprojeet.classes.Role;
import com.example.gestionprojeet.classes.Utlisateur;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticationServiceTest{
        @Test
void testRegister() {
    // Créer tous les mocks nécessaires
    UtlisateurRepo userRepo = mock(UtlisateurRepo.class);
    TokenRepository tokenRepo = mock(TokenRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    JwtService jwtService = mock(JwtService.class);
    AuthenticationManager authManager = mock(AuthenticationManager.class);

    // Créer le service avec tous les mocks injectés
    AuthenticationService authService = new AuthenticationService(
            userRepo, tokenRepo, passwordEncoder, jwtService, authManager
    );

    // Simuler le comportement du passwordEncoder
    when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded123");

    // Simuler le repository et JWT
    when(userRepo.save(any(Utlisateur.class))).thenAnswer(i -> i.getArgument(0));
    when(jwtService.generateToken(any(Utlisateur.class))).thenReturn("jwt-token");
    when(jwtService.gererateRefershToken(any(Utlisateur.class))).thenReturn("refresh-token");

    // Préparer la requête
    RegisterRequest request = new RegisterRequest();
    request.setFirstname("Nour");
    request.setEmail("test@test.com");
    request.setPassword("123456");
    request.setRole(Role.MEMBRE);

    // Appeler la méthode register
    AuthenticationReponse response = authService.register(request);

    // Vérifier le résultat
    assertEquals("jwt-token", response.getAccesToken());
    assertEquals("refresh-token", response.getRefershToken());

    // Vérifier que le save du repo a été appelé
    verify(userRepo, times(1)).save(any(Utlisateur.class));
}

}