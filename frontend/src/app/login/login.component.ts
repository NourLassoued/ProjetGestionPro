import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { Role } from 'src/models/Role';
import { Observable, Subscription } from 'rxjs';
import { IsAdminService } from '../shared/is-admin.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  public loginForm!: FormGroup;
  showLoginForm = true; // Affiche le formulaire de connexion par défaut
  email: string = '';
  password: string = '';
  showResetForm = false; 
  role: Role | undefined;
  isAdmin: boolean = false; 
  profileImage: string | undefined;
  toggleForm() {
    this.showLoginForm = !this.showLoginForm; // Inverse l'état du formulaire
  }
  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router,private isAdminService:IsAdminService) {
    this.loginForm = this.fb.group({
    email: ['', Validators.required],
    password: ['', Validators.required]
  })
  }
  
authenticate(): void {
  this.authService.authenticate(this.email, this.password).subscribe(
    response => {
      console.log('Réponse de l\'authentification dans le composant :', response);

      // Vérifie si la réponse contient un rôle valide
      if (response && response.role && typeof response.role === 'string') {
        this.role = response.role; 

        // Vérifiez si l'utilisateur est administrateur
        this.isAdmin = this.role === 'ADMINISTRATEUR';
        this.isAdminService.setIsAdmin(this.isAdmin);
        
        console.log('Role de l\'utilisateur :', this.role);
        console.log('isAdmin :', this.isAdmin);
        localStorage.setItem('currentUser', JSON.stringify(response));
        // Rediriger vers la page d'accueil après l'authentification
        this.navigateToHome();
      } else {
        console.error('La réponse d\'authentification est invalide ou ne contient pas le rôle.');
        // Gérer le cas où la réponse ne contient pas le rôle
      }
    },
    error => {
      console.error('Erreur lors de l\'authentification :', error);
      // Gérer les erreurs d'authentification ici
    }
  );
}

navigateToHome() {
  // Naviguer vers la page d'accueil
  this.router.navigate(['/home']);
}

  refreshToken(refreshToken: string): void {
    this.authService.refreshToken(refreshToken).subscribe(
      response => {
        // Traitez la réponse du rafraîchissement de token ici
        console.log('Token refreshed', response);
      },
      error => {
        // Gérer les erreurs de rafraîchissement de token ici
        console.error('Refresh token error', error);
      }
    );
  }
  getImageUrl(filename: string): string {
    return `http://localhost:8087/nour/api/v1/auth/get-image/${filename}`;
  }}
