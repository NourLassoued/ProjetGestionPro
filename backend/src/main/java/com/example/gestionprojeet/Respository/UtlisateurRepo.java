package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.Utlisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtlisateurRepo extends JpaRepository<Utlisateur,Long> {
    Optional<Utlisateur>findByEmail(String email);
    @Query("SELECT u.email FROM Utlisateur u")
    List<String> findAllEmails();
    @Transactional
    @Modifying
    @Query("update Utlisateur  u set u.password= ?2 where u.email= ?1")
    void  updatePassword(String email,String password);

}
