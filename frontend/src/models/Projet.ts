import { Carte } from "./Carte";
import { Message } from "./Message";
import { Tableau } from "./Tableau";
import { Utilisateur } from "./Utilisateur";

export class Projet {
    idProjet?: number;
    nom?: string;
    description?: string;
    tableau!: Tableau; 
    cartes!: Carte[];
    utilisateurs!: Utilisateur[]; 
    showFullDescription?: boolean;
    nombreMembres?: number;
    membersList?: any[]; 
    showMembersList?: boolean; 
    messages!:Message[]; 
  }