import { Utilisateur } from "./Utilisateur";

export class Token {
    id?: number;
    token?: string;
    createdAt?: Date; // Adjust types as per your preference
    expiresAt?: Date;
    validatedAt?: Date;
    user!: Utilisateur; // Assuming User is defined similarly as Utilisateur
   
  
  }
  