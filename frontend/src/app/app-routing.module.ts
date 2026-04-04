import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontComponent } from './front/front.component';

import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { PasswordResetComponent } from './password-reset/password-reset.component';
import { CodeOtpComponent } from './code-otp/code-otp.component';
import { BackAdminComponent } from './back-admin/back-admin.component';
import { UserprofileComponent } from './userprofile/userprofile.component';
import { TableuseerComponent } from './tableuseer/tableuseer.component';
import { ProjectuserComponent } from './projectuser/projectuser.component';
import { CarteuserComponent } from './carteuser/carteuser.component';

const routes: Routes = [
{ path: '', redirectTo: '/login', pathMatch: 'full' }, 


  { path: 'home', component: FrontComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'PasswordReset', component: PasswordResetComponent },
  { path: 'CodeOtp',component:CodeOtpComponent},
  
  { path: 'admin',component:BackAdminComponent},
 {path:'userprofile',component:UserprofileComponent},
 {path:'Tableuseer',component:TableuseerComponent},
 {path:'Projectuser',component:ProjectuserComponent},
 {path:'Carteuser',component:CarteuserComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
