package com.example.gestionprojeet.auth;

import com.example.gestionprojeet.Config.JwtService;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.Token.Token;
import com.example.gestionprojeet.Token.TokenRepository;
import com.example.gestionprojeet.Token.TokenType;
import com.example.gestionprojeet.classes.Role;
import com.example.gestionprojeet.classes.Utlisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UtlisateurRepo repository;
    private  final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;



public AuthenticationReponse register(RegisterRequest request) {

    Role role = request.getRole();

    if (role == null) {
        role = Role.getDefaultRole();
    }

    Utlisateur utilisateur = Utlisateur.builder()
            .firstname(request.getFirstname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .image(request.getImage())
            .build();



    var saveUser=  repository.save(utilisateur);
    var jwtToken=jwtService.generateToken(utilisateur);
    var refershToken=jwtService.gererateRefershToken(utilisateur);
    saveUserToken(saveUser, jwtToken);
    return  AuthenticationReponse.builder()
            .accesToken(jwtToken)
            .refershToken(refershToken)
            .build();

}

private  void  revokeAllUserToken(Utlisateur user){
        var valideToken=tokenRepository.findAllValidTokensByUtlisateur(user.getId());
        if(valideToken.isEmpty())
            return;
        valideToken.forEach(t->{
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(valideToken);
    }
    private void saveUserToken(Utlisateur user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    public AuthenticationReponse autheticate (AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user=repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken=jwtService.generateToken(user);
        var refershToken=jwtService.gererateRefershToken(user);
        System.out.println("Role of logged user : " + user.getRole());
        System.out.println("token of logged user : " + jwtToken);
        revokeAllUserToken(user);

        saveUserToken(user,refershToken);
        return  AuthenticationReponse.builder()
                .accesToken(jwtToken)
                .role(user.getRole())
                .build();
    }
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken;
        String userEmail;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
            userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail != null) {
                Utlisateur userDetails = repository.findByEmail(userEmail).orElseThrow();

                if (jwtService.isTokenValid(refreshToken, userDetails)) {
                    String accessToken = jwtService.generateToken(userDetails);
                    revokeAllUserToken(userDetails);
                    saveUserToken(userDetails, accessToken);

                    AuthenticationReponse authResponse = AuthenticationReponse.builder()
                            .accesToken(accessToken)
                            .refershToken(refreshToken)
                            .build();

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(response.getOutputStream(), authResponse);
                }
            }
        }
    }



}
