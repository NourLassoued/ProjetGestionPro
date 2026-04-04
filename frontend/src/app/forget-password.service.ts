import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ForgetPasswordService {
  private baseUrl = 'http://localhost:8086/forgetPassword'; // URL de votre API backend

  constructor(private http: HttpClient) { }


  VerifyEmail(email: string): Observable<string> {
    const url = `${this.baseUrl}/verifyMail/${email}`;
    return this.http.post(url, null, { responseType: 'text' })
      .pipe(
        catchError(this.handleError)
      );
  }


  verifyOtp(otp: number, email: string): Observable<string> {
    const url = `${this.baseUrl}/verifyOtp/${otp}/${email}`;
    return this.http.post(url, null, { responseType: 'text' })
      .pipe(
        catchError(this.handleError)
      );
  }
  private handleError(error: HttpErrorResponse): Observable<string> {
    let errorMessage = 'Unknown error occurred';
    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(errorMessage);
  }



  changePassword(email: string, newPassword: string, repeatPassword: string): Observable<any> {
    const body = { password: newPassword, repeatPassword: repeatPassword };
    const url = `${this.baseUrl}/changePassword/${email}`;
    return this.http.post<string>(url, body, { responseType: 'text' as 'json' }) // Cast 'text' as 'json'
      .pipe(
        catchError(this.handleError)
      );
  }




}
