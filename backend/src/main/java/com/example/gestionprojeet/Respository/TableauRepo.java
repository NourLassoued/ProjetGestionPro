package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.Tableau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableauRepo extends JpaRepository<Tableau,Long> {
}
