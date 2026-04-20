package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Service.MessageService;
import com.example.gestionprojeet.classes.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MessageControlleur {

    private final MessageService messageService;

    @GetMapping("/projet/{idProjet}")
    public ResponseEntity<?> getMessages(@PathVariable Long idProjet) {
        try {
            List<Message> messages = messageService.getMessagesByProjet(idProjet);
            return ResponseEntity.ok(messages);  // 200 OK
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();  // 403 Forbidden
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";
            System.out.println("ERREUR: " + errorMessage);  // ← DEBUG
            e.printStackTrace();

            // 403: Si l'utilisateur n'est pas membre du projet
            if (errorMessage.contains("membre") || errorMessage.contains("Vous n'êtes pas")) {
                return ResponseEntity.status(403).build();  // 403 Forbidden
            }

            // 404: Si le projet n'existe pas
            if (errorMessage.contains("non trouvé")) {
                return ResponseEntity.status(404).build();  // 404 Not Found
            }

            // 401: Si l'utilisateur n'est pas authentifié
            if (errorMessage.contains("authentifié")) {
                return ResponseEntity.status(401).build();  // 401 Unauthorized
            }

            // Défaut: 500
            return ResponseEntity.status(500).build();
        } catch (Exception e) {  // ← CATCH TOUT
            System.out.println("EXCEPTION INATTTENDUE: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    @PostMapping("/projet/{idProjet}")
    public ResponseEntity<Message> envoyerMessage(
            @PathVariable Long idProjet,
            @RequestBody Map<String, String> body) {
        try {
            String contenu = body.get("contenu");
            if (contenu == null || contenu.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            Message message = messageService.envoyerMessage(idProjet, contenu);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";

            // 403: Si l'utilisateur n'est pas membre du projet
            if (errorMessage.contains("membre") || errorMessage.contains("Vous n'êtes pas")) {
                return ResponseEntity.status(403).build();  // 403 Forbidden
            }

            // 404: Si le projet n'existe pas
            if (errorMessage.contains("non trouvé")) {
                return ResponseEntity.status(404).build();  // 404 Not Found
            }

            // 401: Si l'utilisateur n'est pas authentifié
            if (errorMessage.contains("authentifié")) {
                return ResponseEntity.status(401).build();  // 401 Unauthorized
            }

            // Défaut: 500
            return ResponseEntity.status(500).build();
        }
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}