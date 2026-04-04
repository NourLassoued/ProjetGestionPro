package com.example.gestionprojeet.interfacee;

import com.example.gestionprojeet.classes.Utlisateur;

import java.util.List;

public interface UtlisateurInterface {
    public void saveUser(Utlisateur user);
    List<Utlisateur> getAllUser();
    void deleteUser(Long id);
    public List<Utlisateur> recherche(String keyword);
    Utlisateur updateUser(Utlisateur user);
}
