import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ForgetPasswordService } from '../forget-password.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

@Component({
  selector: 'app-code-otp',
  templateUrl: './code-otp.component.html',
  styleUrls: ['./code-otp.component.css']
})
export class CodeOtpComponent implements OnInit {
 
  otp: string[] = ['', '', '', '', '', ''];
  verificationMessage: string = '';
  otpVerified: boolean = false;
  
  email: string = '';
  newPassword: string = '';
  repeatPassword: string = '';
  error: string | null = null;
  successMessage: string | null = null;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
      
    private forgetPasswordService: ForgetPasswordService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.email = params['email'];
    });
  }
  verifyOtp() {
    const otpNumber = parseInt(this.otp.join(''), 10);

    if (!isNaN(otpNumber)) {
      this.forgetPasswordService.verifyOtp(otpNumber, this.email)
        .pipe(
          catchError((error: HttpErrorResponse) => {
            let errorMessage = 'Error verifying OTP. Please try again later.';

            if (error.status === 400) {
              errorMessage = 'Invalid OTP. Please try again.';
            } else if (error.status === 404) {
              errorMessage = 'User not found. Please provide a valid email.';
            }

            this.snackBar.open(errorMessage, 'Close', {
              duration: 5000,
              horizontalPosition: 'center',
              verticalPosition: 'top',
              panelClass: ['error-snackbar']
            });

            return throwError(errorMessage);
          })
        )
        .subscribe(
          (response: any) => {
            this.verificationMessage = response;
            this.otpVerified = true;
            this.snackBar.open('OTP verified successfully!', 'Close', {
              duration: 5000,
              horizontalPosition: 'center',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
          },
          (error: any) => {
            console.error('Error verifying OTP:', error);
            this.snackBar.open('Error verifying OTP. Please try again later.', 'Close', {
              duration: 5000,
              horizontalPosition: 'center',
              verticalPosition: 'top',
              panelClass: ['error-snackbar']
            });
          }
        );
    } else {
      this.snackBar.open('Please enter a valid OTP.', 'Close', {
        duration: 5000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
    }
  } changePassword() {
    if (this.newPassword !== this.repeatPassword) {
      this.error = 'Passwords do not match. Please try again.';
      return;
    }

    this.error = null;

    this.forgetPasswordService.changePassword(this.email, this.newPassword, this.repeatPassword)
      .subscribe(
        () => {
          this.successMessage = 'Password has been changed successfully!';
          // Rediriger vers la page de connexion après 2 secondes
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000); // Redirection après 2 secondes (2000 millisecondes)
        },
        (error) => {
          this.error = 'Error changing password: ' + error;
          this.successMessage = null;
        }
      );
  }
}