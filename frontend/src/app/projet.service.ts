import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, tap } from 'rxjs';
import { Carte } from 'src/models/Carte';
import { Projet } from 'src/models/Projet';

@Injectable({
  providedIn: 'root'
})
export class ProjetService {
  private baseUrl = 'http://localhost:8086/api/v1/auth';
  constructor(private http: HttpClient){ }
  
  createProject(idTableau: number, projet: Projet): Observable<Projet> {
    const url = `${this.baseUrl}/create/${idTableau}`;
    return this.http.post<Projet>(url, projet);
     
} getAllProjects(): Observable<any> {
  return this.http.get<any>(`${this.baseUrl}/getAllProjects`)
}
ajouterUtilisateurAuProjetParEmail(email: string, idProjet: number): Observable<any> {
  return this.http.post(`${this.baseUrl}/${email}/projets/${idProjet}`, null);
}
getProjetById(id: number): Observable<Projet> {
  return this.http.get<Projet>(`${this.baseUrl}/${id}`);
}

ajouterCarteAProjet(idProjet: number, carte: any): Observable<string> {
  const url = `${this.baseUrl}/${idProjet}/cartes`;
  return this.http.post<string>(url, carte);
}

// Méthode pour ajouter une carte à un projet pour un utilisateur
ajouterCarteAuProjetPourUtilisateur(id: number, idProjet: number, carte: any): Observable<string> {
  const url = `${this.baseUrl}/${id}/ajouterCarte/${idProjet}`;
  return this.http.post<string>(url, carte);
} 
getProjetsUtilisateur(id: number): Observable<any[]> {
  const url = `${this.baseUrl}/${id}/projets`;
  return this.http.get<any[]>(url);
}
getCartesDuProjet(idProjet: number): Observable<Carte[]> {
  const url = `${this.baseUrl}/${idProjet}/cartes`;
  return this.http.get<Carte[]>(url);
}
  updateCarteStatut(idCarte: number, statut: string): Observable<Carte> {
    return this.http.put<Carte>(
      `${this.baseUrl}/cartes/${idCarte}/statut?statut=${statut}`, null
    );
  }
}

