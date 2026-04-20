package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Utlisateur;
import com.example.gestionprojeet.Service.Utlisateurservice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor


public class UtlisateurControlleur {
    @Autowired
     Utlisateurservice userService;
    private  UtlisateurRepo userRepository;
    @PostMapping("/usersave")
    public ResponseEntity<String> saveUser(@RequestBody Utlisateur user) {
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur ajouté avec succès");
    }
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<Utlisateur> updateUser(@PathVariable long id, @RequestBody Utlisateur utlisateur) {
        utlisateur.setId(id);  // ← IMPORTANT: Passe l'ID au service
        return ResponseEntity.ok(userService.updateUser(utlisateur));
    }


    @DeleteMapping("/deleteUser/{id}")


    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build(); // Retourne 200 OK si la suppression réussit
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            // Retourne une réponse 500 en cas d'erreur interne
        }
    }



    @GetMapping("/getAllUser")
    public List<Utlisateur> getAllUsers(){
        return userService.getAllUser();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }}