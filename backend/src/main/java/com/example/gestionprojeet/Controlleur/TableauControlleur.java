package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.classes.Tableau;
import com.example.gestionprojeet.Service.Tableauservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

@CrossOrigin(origins = "http://localhost:4200")
public class TableauControlleur {


        private final Tableauservice tableauService;

        @PostMapping("/tableaux/{id}")
        public ResponseEntity<Tableau> createTableau(
                @PathVariable Long id,
                @RequestBody Tableau tableau) {
            Tableau created = tableauService.createTableau(id, tableau);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        }

        @PostMapping("/tableaux/byEmail/{email}")
        public ResponseEntity<Tableau> createTableauByEmail(
                @PathVariable String email,
                @RequestBody Tableau tableau) {
            Tableau created = tableauService.createTableauByEmail(email, tableau);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        }

        @GetMapping("/tableaux/utilisateur/{id}")
        public ResponseEntity<List<Tableau>> getTableauxByUtilisateur(@PathVariable Long id) {
            return ResponseEntity.ok(tableauService.getTableauxByUtilisateur(id));
        }

        @GetMapping("/tableaux/byEmail/{email}")
        public ResponseEntity<List<Tableau>> getTableauxByEmail(@PathVariable String email) {
            return ResponseEntity.ok(tableauService.getTableauxByEmail(email));
        }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
    }

