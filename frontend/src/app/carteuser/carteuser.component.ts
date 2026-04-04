import { Component, OnInit } from '@angular/core';
import { ProjetService } from '../projet.service';
import { Utilisateur } from 'src/models/Utilisateur';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { IsAdminService } from '../shared/is-admin.service';
import { FileService } from '../file.service';
import { UserService } from '../user.service';
import { TableauxService } from '../tableaux.service';
import { Tableau } from 'src/models/Tableau';
import { Carte } from 'src/models/Carte';
import { Projet } from 'src/models/Projet';

@Component({
  selector: 'app-carteuser',
  templateUrl: './carteuser.component.html',
  styleUrls: ['./carteuser.component.css']
})
export class CarteuserComponent implements OnInit {
constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private isAdminService: IsAdminService,
    private fileService: FileService,
    private userService: UserService,
    private tableauService: TableauxService,
    private projetService: ProjetService
  ) {}
 
  status = false;
  userForm: Utilisateur = {} as Utilisateur;
  showUpdateForm: boolean = false;
  currentUser: Utilisateur | null = null;
  currentEmail: string = '';
  profileImage: string | undefined;
  isAdmin: boolean = false;
  tableForm: Tableau = { nom: '', description: '', proprietaire: null, projets: [] };
  displayTableForm: boolean = false;
  cartesDuProjet: Carte[] = [];
  projetsUtilisateur: Projet[] = [];
  projetSelectionne: Projet | null = null;
 
  // Kanban columns
  cartesAFaire: Carte[] = [];
  cartesEnCours: Carte[] = [];
  cartesTermine: Carte[] = [];
 
  // Drag state
  draggedCarte: Carte | null = null;
 
  nouvelleCarte: Carte = {
    titre: '',
    description: '',
    projet: null,
    commentaires: []
  };
 
  ngOnInit(): void {
    this.checkUserRole();
    this.loadUserData();
  }
 
  private getEmailFromToken(): string {
    const token = localStorage.getItem('access_token');
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(base64)).sub || '';
      } catch (e) {}
    }
    return '';
  }
 
  loadUserData(): void {
    this.currentEmail = this.getEmailFromToken();
    if (!this.currentEmail) {
      this.router.navigate(['/login']);
      return;
    }
 
    const currentUserString = localStorage.getItem('currentUser');
    let stored: any = {};
    if (currentUserString) {
      try { stored = JSON.parse(currentUserString); } catch (e) {}
    }
    this.currentUser = { ...stored, email: this.currentEmail } as Utilisateur;
    this.setupUserUI();
    this.loadProjetsUtilisateur();
  }
 
  private setupUserUI(): void {
    if (this.currentUser) {
      if (this.currentUser.image) {
        this.profileImage = this.getImageUrl(this.currentUser.image);
      }
      this.userForm.firstname = this.currentUser.firstname || '';
      this.userForm.email = this.currentUser.email || '';
    }
  }
 
  private checkUserRole(): void {
    this.isAdminService.getIsAdmin().subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });
  }
 
  getImageUrl(filename: string): string {
    return `http://localhost:8086/api/v1/auth/get-image/${filename}`;
  }
 
  onSubmit(): void {
    if (this.currentUser && this.currentUser.id) {
      this.userService.updateUser(this.currentUser.id, this.userForm as Utilisateur).subscribe(
        (updatedUser) => {
          if (updatedUser) {
            this.currentUser = updatedUser;
            this.userForm.firstname = updatedUser.firstname || '';
            this.userForm.email = updatedUser.email || '';
            localStorage.setItem('currentUser', JSON.stringify(updatedUser));
          }
        }
      );
    }
  }
 
  toggleUpdateForm(): void { this.showUpdateForm = !this.showUpdateForm; }
 
  logout(): void {
    this.authService.logout();
    localStorage.removeItem('access_token');
    localStorage.removeItem('currentUser');
    this.router.navigate(['/login']);
  }
 
  addToggle(): void { this.status = !this.status; }
  showTableForm(): void { this.displayTableForm = true; }
  toggleTableForm(): void { this.displayTableForm = !this.displayTableForm; }
 
  onSubmitTableau(): void {
    if (this.currentUser && this.currentUser.id) {
      this.tableauService.createTableau(this.currentUser.id, this.tableForm).subscribe(
        () => {
          this.tableForm = { nom: '', description: '', proprietaire: null, projets: [] };
          this.displayTableForm = false;
        }
      );
    }
  }
 
  loadProjetsUtilisateur(): void {
    if (!this.currentEmail) return;
    this.projetService.getAllProjects().subscribe(
      (allProjects: Projet[]) => {
        this.projetsUtilisateur = allProjects.filter(projet =>
          projet.utilisateurs &&
          projet.utilisateurs.some((user: any) =>
            user.email?.toLowerCase() === this.currentEmail.toLowerCase()
          )
        );
      }
    );
  }
 
  selectionnerProjet(projet: Projet): void {
    this.projetSelectionne = projet;
    if (projet.idProjet) {
      this.loadCartesDuProjet(projet.idProjet);
    }
  }
 
  loadCartesDuProjet(idProjet: number): void {
    this.projetService.getCartesDuProjet(idProjet).subscribe(
      (cartes: Carte[]) => {
        this.cartesDuProjet = cartes;
        this.organiserCartesParStatut();
      }
    );
  }
 
  /** Répartir les cartes dans les 3 colonnes Kanban */
  organiserCartesParStatut(): void {
    this.cartesAFaire = this.cartesDuProjet.filter(c => !c.statut || c.statut === 'A_FAIRE');
    this.cartesEnCours = this.cartesDuProjet.filter(c => c.statut === 'EN_COURS');
    this.cartesTermine = this.cartesDuProjet.filter(c => c.statut === 'TERMINE');
  }
 
  ajouterCarteAProjet(): void {
    if (this.projetSelectionne && this.projetSelectionne.idProjet) {
      this.projetService.ajouterCarteAProjet(this.projetSelectionne.idProjet, this.nouvelleCarte).subscribe(
        () => {
          this.nouvelleCarte = { titre: '', description: '', projet: null, commentaires: [] };
          if (this.projetSelectionne?.idProjet) {
            this.loadCartesDuProjet(this.projetSelectionne.idProjet);
          }
        }
      );
    }
  }
 
  // ─── DRAG & DROP ─────────────────────────────────────────────
  onDragStart(carte: Carte): void {
    this.draggedCarte = carte;
  }
 
  onDragOver(event: DragEvent): void {
    event.preventDefault(); // Nécessaire pour autoriser le drop
  }
 
  onDrop(event: DragEvent, nouveauStatut: string): void {
    event.preventDefault();
    if (this.draggedCarte && this.draggedCarte.idCarte) {
      const ancienStatut = this.draggedCarte.statut;
      if (ancienStatut !== nouveauStatut) {
        // Mise à jour optimiste côté frontend
        this.draggedCarte.statut = nouveauStatut;
        this.organiserCartesParStatut();
 
        // Appel API backend
        this.projetService.updateCarteStatut(this.draggedCarte.idCarte, nouveauStatut).subscribe(
          () => {
            console.log('Statut mis à jour:', nouveauStatut);
          },
          (error) => {
            console.error('Erreur mise à jour statut:', error);
            // Rollback si erreur
            if (this.draggedCarte) {
              this.draggedCarte.statut = ancienStatut;
              this.organiserCartesParStatut();
            }
          }
        );
      }
    }
    this.draggedCarte = null;
  }
 
  onDragEnd(): void {
    this.draggedCarte = null;
  }
}