import { Component } from '@angular/core';
import { ForgetPasswordService } from '../forget-password.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.css']
})
export class PasswordResetComponent {

 
  
  constructor(private forgetPasswordService: ForgetPasswordService, private router: Router,) {}


  
  email: string = '';
  message: string = '';
  otp: string = ''; 
  showOTPForm: boolean = false; 
  sendEmail(): void {
    this.forgetPasswordService.VerifyEmail(this.email)
      .subscribe(
        response => {
          console.log('Email sent:', response); // Afficher la réponse reçue depuis l'API
          this.message = response; // Mettre à jour le message avec la réponse reçue
          this.navigateToCodeOtp();
        },
        error => {
          console.error('Error sending OTP:', error);
          this.message = 'Error sending OTP: ' + error.message; // Afficher un message d'erreur
        }
      );
  }

  navigateToCodeOtp(): void {
    this.router.navigate(['/CodeOtp', { email: this.email }]);
  }
}
    