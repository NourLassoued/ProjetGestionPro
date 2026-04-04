// forgot-password.model.ts

import { Utilisateur } from "./Utilisateur";

export class ForgotPassword {
    fpid?: number;
    otp?: number;
    expirationTime?: Date;
    user!: Utilisateur; // Define Utlisateur interface or class as needed
  }
  