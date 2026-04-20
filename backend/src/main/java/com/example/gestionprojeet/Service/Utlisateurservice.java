package com.example.gestionprojeet.Service;

import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.Utlisateur;
import com.example.gestionprojeet.interfacee.UtlisateurInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Utlisateurservice implements UtlisateurInterface {
    @Autowired
    public UtlisateurRepo userRepository;
    @Override
    public void saveUser(Utlisateur user) {
        userRepository.save(user);
    }

    @Override
    public List<Utlisateur> getAllUser() {
        return userRepository.findAll();

    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<Utlisateur> recherche(String keyword) {
        return null;
    }


    @Override
    public Utlisateur updateUser(Utlisateur user) {
        // ✅ CORRIGÉ : Utilise orElseThrow au lieu de .get()
        Utlisateur existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + user.getId()));

        existingUser.setFirstname(user.getFirstname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());

        Utlisateur updatedUser = userRepository.save(existingUser);
        return updatedUser;
    }

}

