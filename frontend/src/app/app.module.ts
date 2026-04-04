import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MatSnackBarModule } from '@angular/material/snack-bar'; 
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FrontComponent } from './front/front.component';
import { LoginComponent } from './login/login.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { RegisterComponent } from './register/register.component';
import { HttpClientModule } from '@angular/common/http';
import { PasswordResetComponent } from './password-reset/password-reset.component';
import { PasswordResettComponent } from './password-resett/password-resett.component';
import { CodeOtpComponent } from './code-otp/code-otp.component';
import { BackAdminComponent } from './back-admin/back-admin.component';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { UserprofileComponent } from './userprofile/userprofile.component';
import { TableuseerComponent } from './tableuseer/tableuseer.component';
import { ProjectuserComponent } from './projectuser/projectuser.component';
import { CarteuserComponent } from './carteuser/carteuser.component';


@NgModule({
  declarations: [
    AppComponent,
    FrontComponent,
    LoginComponent,
    RegisterComponent,
    PasswordResetComponent,
    PasswordResettComponent,
    CodeOtpComponent,
    BackAdminComponent,
    UserprofileComponent,
    TableuseerComponent,
    ProjectuserComponent,
    CarteuserComponent,
  
 
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatTableModule,
    FormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatToolbarModule,
    HttpClientModule,
    MatSnackBarModule, // Importez MatSnackBarModule ici
    ReactiveFormsModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule
  
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
