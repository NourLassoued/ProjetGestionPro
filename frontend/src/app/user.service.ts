import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Observable } from 'rxjs/internal/Observable';
import { Utilisateur } from 'src/models/Utilisateur';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = 'http://localhost:8086/api/v1/auth';
  constructor(private http: HttpClient, private router: Router, private snackBar: MatSnackBar) { }

  getAllUser():Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.baseUrl}/getAllUser`);
}
/*
deleteUser(id : number): Observable<Utilisateur> {
    return this.http.delete<Utilisateur>(`${this.baseUrl}/deleteUser/{id}`)
}*/
deleteUser(id: number): Observable<any> {
  const url = `${this.baseUrl}/deleteUser/${id}`;

  return this.http.delete(url).pipe(
    catchError((error) => {
      console.error('Error deleting user:', error);
      this.snackBar.open('Error deleting user', 'Close', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom'
      });
      return throwError(error); // Renvoie l'erreur pour la gestion dans le composant
    })
  );
}

changerRoleUtilisateur(id: number, newRole: string): Observable<Utilisateur> {
  const url = `${this.baseUrl}/${id}/changer-role/${newRole}`;
  return this.http.put<Utilisateur>(url, null); // Envoyer la requête PUT, null pour le corps
} 
updateUser(id: number, updatedUser: Utilisateur): Observable<Utilisateur> {
  const url = `${this.baseUrl}/${id}/update`;
  return this.http.put<Utilisateur>(url, updatedUser);
}
getAllUserEmails(): Observable<string[]> {
  return this.http.get<string[]>(`${this.baseUrl}/emails`);
}

}




