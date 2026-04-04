import { Carte } from "./Carte";


export class Commentaire {
  idCommentaire?: number;
  contenu?: string;
  dateCreation?: Date;
  carte!: Carte; // Assurez-vous que l'interface Carte correspond à la structure
}
