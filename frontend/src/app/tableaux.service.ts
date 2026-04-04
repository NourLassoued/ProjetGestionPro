import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Tableau } from 'src/models/Tableau';

@Injectable({
  providedIn: 'root'
})
export class TableauxService {
  private baseUrl = 'http://localhost:8086/api/v1/auth';
  constructor(private http: HttpClient, private router: Router) { } 
 createTableau(id: number, tableau: Tableau): Observable<Tableau> {
    const url = `${this.baseUrl}/tableaux/${id}`;
    return this.http.post<Tableau>(url, tableau);
  }
 
  // Créer par email
  createTableauByEmail(email: string, tableau: Tableau): Observable<Tableau> {
    const url = `${this.baseUrl}/tableaux/byEmail/${email}`;
    return this.http.post<Tableau>(url, tableau);
  }
 
  // Récupérer par ID
  getTableauxByUtilisateur(id: number): Observable<Tableau[]> {
    const url = `${this.baseUrl}/tableaux/utilisateur/${id}`;
    return this.http.get<Tableau[]>(url);
  }
 
  // Récupérer par email
  getTableauxByEmail(email: string): Observable<Tableau[]> {
    const url = `${this.baseUrl}/tableaux/byEmail/${email}`;
    return this.http.get<Tableau[]>(url);
  }

}


