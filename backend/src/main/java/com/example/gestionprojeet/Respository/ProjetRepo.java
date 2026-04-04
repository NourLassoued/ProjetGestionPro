package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepo extends JpaRepository<Projet,Long> {
    List<Projet> findByUtilisateursId(Long userId);
}
