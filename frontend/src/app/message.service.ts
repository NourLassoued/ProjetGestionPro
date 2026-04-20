import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Message } from 'src/models/Message';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private baseUrl = 'http://localhost:8086/api/v1/auth/messages';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getMessages(idProjet: number): Observable<Message[]> {
    return this.http.get<Message[]>(
      `${this.baseUrl}/projet/${idProjet}`,
      { headers: this.getHeaders() }
    );
  }

  envoyerMessage(idProjet: number, contenu: string): Observable<Message> {
    return this.http.post<Message>(
      `${this.baseUrl}/projet/${idProjet}`,
      { contenu },
      { headers: this.getHeaders() }
    );
  }
}