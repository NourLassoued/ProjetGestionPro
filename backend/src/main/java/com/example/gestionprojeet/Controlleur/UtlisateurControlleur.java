package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Utlisateur;
import com.example.gestionprojeet.service.Utlisateurservice;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Utlisateur>  updateUser(@PathVariable("id") long id, @RequestBody Utlisateur user){
        user.setId(id);
        Utlisateur updatedUser = userService.updateUser(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/deleteUser/{id}")
/*

    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build(); // Retourne 200 OK si la suppression réussit
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            // Retourne une réponse 500 en cas d'erreur interne
        }
    }
/*

 */

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
}
