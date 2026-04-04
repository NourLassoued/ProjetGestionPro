import { Component } from '@angular/core';
import { Tableau } from 'src/models/Tableau';
import { Utilisateur } from 'src/models/Utilisateur';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { IsAdminService } from '../shared/is-admin.service';
import { FileService } from '../file.service';
import { UserService } from '../user.service';
import { TableauxService } from '../tableaux.service';
import { ProjetService } from '../projet.service';
import { Projet } from 'src/models/Projet';

@Component({
  selector: 'app-tableuseer',
  templateUrl: './tableuseer.component.html',
  styleUrls: ['./tableuseer.component.css']
})
export class TableuseerComponent {
  status = false;
  profileImage: string | undefined;
  users: Utilisateur[] = [];
  tableaux: Tableau[] = [];
  tableau: Tableau | null = null;
  isAdmin: boolean = false;
  currentUser: Utilisateur | null = null;
  currentEmail: string = '';
  displayTableForm: boolean = false;
  userForm: Utilisateur = {} as Utilisateur;
  showUpdateForm: boolean = false;
  showForm: boolean = false;
  projects: any[] = [];
  newProject: Projet = new Projet();
  selectedTableauId: number | null = null;
  tableForm: Tableau = { nom: '', description: '', proprietaire: null, projets: [] };
 
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
 
  ngOnInit(): void {
    this.checkUserRole();
    this.loadUserData();
  }
 
  /** Extraire l'email depuis le JWT token */
  private getEmailFromToken(): string {
    const token = localStorage.getItem('access_token');
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const decoded = JSON.parse(atob(base64));
        return decoded.sub || '';
      } catch (e) {
        console.error('Erreur décodage JWT:', e);
      }
    }
    return '';
  }
 
  loadUserData(): void {
    this.currentEmail = this.getEmailFromToken();
 
    if (!this.currentEmail) {
      console.error('Aucun token valide.');
      this.router.navigate(['/login']);
      return;
    }
 
    console.log('Email extrait du JWT :', this.currentEmail);
 
    const currentUserString = localStorage.getItem('currentUser');
    let stored: any = {};
    if (currentUserString) {
      try { stored = JSON.parse(currentUserString); } catch (e) {}
    }
 
    this.currentUser = { ...stored, email: this.currentEmail } as Utilisateur;
 
    if (this.currentUser && this.currentUser.image) {
      this.profileImage = this.getImageUrl(this.currentUser.image);
    }
    this.userForm.firstname = this.currentUser?.firstname || '';
    this.userForm.email = this.currentEmail;
 
    // Charger les tableaux et projets
    this.fetchUserTableaux();
    this.getAllProjects();
  }
 
  private checkUserRole(): void {
    this.isAdminService.getIsAdmin().subscribe(isAdmin => {
      this.isAdmin = isAdmin;
      console.log('isAdmin:', this.isAdmin);
    });
    const currentUserString = localStorage.getItem('currentUser');
    if (currentUserString) {
      try {
        const currentUser = JSON.parse(currentUserString);
        if (currentUser && currentUser.image) {
          this.profileImage = this.getImageUrl(currentUser.image);
        }
      } catch (e) {}
    }
  }
 
  getImageUrl(filename: string): string {
    return `http://localhost:8086/api/v1/auth/get-image/${filename}`;
  }
 
  addToggle(): void {
    this.status = !this.status;
  }
 
  toggleUpdateForm(): void {
    this.showUpdateForm = !this.showUpdateForm;
  }
 
  logout(): void {
    this.authService.logout();
    localStorage.removeItem('access_token');
    localStorage.removeItem('currentUser');
    this.router.navigate(['/login']);
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
        },
        (error) => console.error('Erreur mise à jour :', error)
      );
    }
  }
 
  // ─── TABLEAUX (par email, pas besoin de l'id) ────────────────
 
  showTableForm(): void {
    this.displayTableForm = true;
  }
 
  toggleTableForm(): void {
    this.displayTableForm = !this.displayTableForm;
  }
 
  /** Créer un tableau — utilise l'email extrait du JWT */
  onSubmitTableau(): void {
    if (!this.currentEmail) {
      console.error('Email non défini.');
      return;
    }
 
    this.tableauService.createTableauByEmail(this.currentEmail, this.tableForm).subscribe(
      (newTableau: Tableau) => {
        console.log('Tableau créé :', newTableau);
        this.tableaux.push(newTableau);
        this.tableForm = { nom: '', description: '', proprietaire: null, projets: [] };
        this.displayTableForm = false;
      },
      (error: any) => {
        console.error('Erreur création tableau :', error);
      }
    );
  }
 
  /** Récupérer les tableaux — utilise l'email extrait du JWT */
  fetchUserTableaux(): void {
    if (!this.currentEmail) {
      console.error('Email non défini.');
      return;
    }
 
    this.tableauService.getTableauxByEmail(this.currentEmail).subscribe(
      (tableaux: Tableau[]) => {
        this.tableaux = tableaux;
        console.log('Tableaux récupérés :', this.tableaux);
      },
      (error) => {
        console.error('Erreur récupération tableaux :', error);
      }
    );
  }
 
  // ─── PROJETS ─────────────────────────────────────────────────
 
  showProjectForm(tableau: Tableau): void {
    if (tableau && tableau.idTableau !== undefined) {
      if (this.selectedTableauId === tableau.idTableau) {
        this.selectedTableauId = null;
        this.showForm = false;
      } else {
        this.selectedTableauId = tableau.idTableau;
        this.showForm = true;
      }
    }
  }
 
  onSubmitProject(): void {
    if (this.selectedTableauId !== null) {
      const selectedTableau = this.tableaux.find(t => t.idTableau === this.selectedTableauId);
 
      if (selectedTableau) {
        this.newProject.tableau = selectedTableau;
 
        this.projetService.createProject(this.selectedTableauId, this.newProject).subscribe(
          (createdProject) => {
            console.log('Projet créé :', createdProject);
            this.newProject = new Projet();
            this.showForm = false;
            this.selectedTableauId = null;
            this.getAllProjects();
          },
          (error) => console.error('Erreur création projet :', error)
        );
      }
    }
  }
 
  getAllProjects(): void {
    this.projetService.getAllProjects().subscribe(
      data => { this.projects = data; },
      error => console.error('Error fetching projects:', error)
    );
  }
}
 