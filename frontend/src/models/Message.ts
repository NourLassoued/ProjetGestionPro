export interface Message {
  idMessage?: number;
  contenu: string;
  dateEnvoi?: Date;
  expediteur?: {
    id?: number;
    firstname?: string;
    email?: string;
    image?: string;
    role?: string;
  };
}