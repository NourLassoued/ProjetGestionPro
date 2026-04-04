package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface commentaire extends JpaRepository<Commentaire,Long> {
}
