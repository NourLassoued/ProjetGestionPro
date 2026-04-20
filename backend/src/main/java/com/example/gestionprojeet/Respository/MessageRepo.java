package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {
    List<Message> findByProjetIdProjetOrderByDateEnvoiAsc(Long idProjet);
}