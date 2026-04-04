package com.example.gestionprojeet.Respository;

import com.example.gestionprojeet.classes.ForgotPassword;
import com.example.gestionprojeet.classes.Utlisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ForgetPasswordRepository extends JpaRepository<ForgotPassword,Integer> {
    @Query("select fp from ForgotPassword  fp where  fp.otp= ?1 and fp.user= ?2")
    Optional<ForgotPassword>findByOtpAndUtlisateur(Integer otp, Utlisateur user);
}
