import { Injectable } from '@angular/core';
import { Utilisateur } from 'src/models/Utilisateur';
import {BehaviorSubject, map, Observable, tap} from "rxjs";
import { Router } from '@angular/router';
import {HttpClient, HttpHeaders} from "@angular/common/http";
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  public userSubject: BehaviorSubject<Utilisateur | null>;
  public user: Observable<Utilisateur | null>;
  isconn: any=false;
  private baseUrl = 'http://localhost:8086/api/v1/auth';

  constructor(private http: HttpClient, private router: Router) {
    this.userSubject = new BehaviorSubject(JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user')!));
    this.user = this.userSubject.asObservable();
  }
  public get userValue() {
    return this.userSubject.value;
  }
  isLoggedInWithCredentials(email: string, password: string): boolean {
    const storedEmail = localStorage.getItem('email');
    const storedPassword = localStorage.getItem('password');
    console.log('Stored email and password:', storedEmail, storedPassword);
    return storedEmail === email && storedPassword === password;
  }

  logout(): void {
  
    localStorage.removeItem('email'); // Supprimer l'email stocké
    localStorage.removeItem('password'); // Supprimer le mot de passe
    this.router.navigate(['/login']); // Rediriger vers la page de connexion
  }
 

  register(data: any) {
    return this.http.post<any>(`${this.baseUrl}/register`, data);
  }
 
  getAuthToken(): string {
    const token = localStorage.getItem('access_token');
    console.log('SERVICE token is' + token)

    return token || 'EMPTY';
  }
  getAccessToken(): string {
    return localStorage.getItem('accessToken')!;
  }/*
  authenticate(email: string, password: string): Observable<any> {
    const body = { email, password };
    console.log('AuthService: authenticate called with email', email);
    return this.http.post<any>(`${this.baseUrl}/authenticate`, body);
  }
*/
authenticate(email: string, password: string): Observable<any> {
  const body = { email, password };
  return this.http.post<any>(`${this.baseUrl}/authenticate`, body)
    .pipe(
      tap(response => {
        
        localStorage.setItem('access_token', response.access_token);
        console.log('Token d\'accès stocké dans localStorage :', response.access_token);
      })
    ); // Ajout de la parenthèse fermante pour le pipe()
}
    

// Autres méthodes du service

  refreshToken(refreshToken: string): Observable<any> {
    const body = { refreshToken };
    return this.http.post<any>(`${this.baseUrl}/refreshToken`, body);
  }
  getUserRole(): string | null {
    // Exemple: récupération du rôle depuis le token ou toute autre source
    const accessToken = localStorage.getItem('access_token');
    if (accessToken) {
      // Parsez le token et extrayez le rôle, ou utilisez une méthode appropriée pour le récupérer
      const decodedToken = this.decodeAccessToken(accessToken);
      return decodedToken.role; // Suppose que le token contient le rôle de l'utilisateur
    }
    return null;
  }

  private decodeAccessToken(token: string): any {
    // Implémentez la logique pour décoder le token JWT ou autre format de token
    // Exemple simplifié: vous pouvez utiliser jwt-decode ou une bibliothèque similaire
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  }



  isLoggedIn(): boolean {
    // Vérifiez ici si l'utilisateur est authentifié en vérifiant la présence du token
    const token = localStorage.getItem('access_token');
    return !!token; // Retourne true si le token existe, false sinon
  }
  getCurrentUser(): Observable<any> {
    const accessToken = localStorage.getItem('access_token');

    if (accessToken) {
      // Envoyer une requête GET pour récupérer les détails de l'utilisateur
      return this.http.get<any>(`${this.baseUrl}/current-user`, {
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
      });
    } else {
      // Gérer le cas où le token n'est pas trouvé dans localStorage
      return new Observable(observer => {
        observer.error('Token d\'accès introuvable dans le localStorage.');
      });
    }
  }
  private isAdmin: boolean = false;

  setIsAdmin(isAdmin: boolean) {
    this.isAdmin = isAdmin;
  }

  getIsAdmin(): boolean {
    return this.isAdmin;
  }
  
}
