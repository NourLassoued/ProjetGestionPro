import { ForgotPassword } from "./ForgotPassword";
import { Projet } from "./Projet";
import { Role } from "./Role";
import { Tableau } from "./Tableau";
import { Token } from "./Token";

export class Utilisateur {
    id?: number;
    firstname?: string;
    lastname?: string;
    email?: string;
    password?: string;
     image?:string;
    role?: Role; // Ensure Role is defined in Angular if not directly imported
    tokens!: Token[];
    tableaux!: Tableau[];
    projets!: Projet[];
    forgotPassword!: ForgotPassword;
  
  }
  