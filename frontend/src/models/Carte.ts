import { Commentaire } from "./Commentaire";
import { Projet } from "./Projet";

export class Carte {
  idCarte?: number;
  titre?: string;
  description?: string;
   statut?: string; 
 auteur?: {
    id?: number;
    firstname?: string;
    email?: string;
    image?: string;
    role?: string;
  };
 dateCreation?: Date;   
  dateEcheance?: Date;
  projet!: Projet | null; ;
  commentaires!: Commentaire[]; 
}
