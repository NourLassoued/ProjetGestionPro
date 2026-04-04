package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.Carte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarteRepo extends JpaRepository<Carte,Long> {
    List<Carte> findByProjetIdProjet(Long idProjet);

}
