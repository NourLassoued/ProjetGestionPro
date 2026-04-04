package com.example.gestionprojeet.Controlleur;

import com.example.gestionprojeet.Respository.ForgetPasswordRepository;
import com.example.gestionprojeet.Respository.UtlisateurRepo;
import com.example.gestionprojeet.classes.ChangePassword;
import com.example.gestionprojeet.classes.ForgotPassword;
import com.example.gestionprojeet.classes.MailBody;
import com.example.gestionprojeet.classes.Utlisateur;
import com.example.gestionprojeet.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/forgetPassword")
public class ForgetPasswordController {
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private   UtlisateurRepo utlisateurRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ForgetPasswordRepository forgetPasswordRepository;
/*
    @PostMapping("verifyMail/{email}")
    public ResponseEntity<String> veriFyEmail(@PathVariable String email){
        Utlisateur user=utlisateurRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("Please provide an valid email !"));
        int otp=OtpGenrator();
        MailBody mailBody =MailBody.builder().to(email)
                .text("this is the the OTP for your Forget Password :"+otp)
                .subject("OTP for Forget Password request")
                .build();
        ForgotPassword fp=ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis()+ 70 * 1000))
                .user(user)
                .build();
        emailService.setJavaMailSender(mailBody);
        forgetPasswordRepository.save(fp);
        return  ResponseEntity.ok("Email sent for  verfication!");
    }*/
@PostMapping("/verifyMail/{email}")
public ResponseEntity<String> verifyEmail(@PathVariable String email) {
    try {
        // Rechercher l'utilisateur par email
        Utlisateur user = utlisateurRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email !"));

        // Générer un OTP
        int otp =OtpGenrator();

        // Préparer le contenu du mail
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("This is the OTP for your Forget Password: " + otp)
                .subject("OTP for Forget Password request")
                .build();

        // Sauvegarder l'OTP dans la base de données
        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .user(user)
                .build();
        forgetPasswordRepository.save(fp);

        // Envoyer l'email
        emailService.setJavaMailSender(mailBody);

        // Retourner une réponse de confirmation
        return ResponseEntity.ok("Email sent for verification!");
    } catch (UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email: " + ex.getMessage());
    }
}
/*
    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        Utlisateur user = utlisateurRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        ForgotPassword fp = forgetPasswordRepository.findByOtpAndUtlisateur(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email"));

        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgetPasswordRepository.deleteById(fp.getFpid());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Otp has expired!");
        }

        return ResponseEntity.ok("OTP verified!");
    }

*/
@PostMapping("/verifyOtp/{otp}/{email}")
public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
    try {
        // Rechercher l'utilisateur par email
        Utlisateur user = utlisateurRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        // Rechercher l'entrée OTP correspondante dans la base de données
        ForgotPassword fp = forgetPasswordRepository.findByOtpAndUtlisateur(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email"));

        // Vérifier si l'OTP a expiré
        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgetPasswordRepository.deleteById(fp.getFpid());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("OTP has expired!");
        }

        // Supprimer l'entrée OTP après vérification réussie (optionnel)
        forgetPasswordRepository.deleteById(fp.getFpid());

        // Retourner une réponse de confirmation
        return ResponseEntity.ok("OTP verified!");
    } catch (UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying OTP: " + ex.getMessage());
    }
}
    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword, @PathVariable String email) {
        try {
            if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match. Please try again!");
            }

            String encodedPassword = passwordEncoder.encode(changePassword.password());
            utlisateurRepo.updatePassword(email, encodedPassword);
            return ResponseEntity.ok("Password has been changed!");
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing password: " + ex.getMessage());
        }
    }
    private Integer OtpGenrator(){
        Random random =new Random();
        return random.nextInt(100_000,999_999);
    }
}