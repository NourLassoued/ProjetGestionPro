import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { IsAdminService } from '../shared/is-admin.service';
import { FileService } from '../file.service';
import { UserService } from '../user.service';
import { Utilisateur } from 'src/models/Utilisateur';
import { Tableau } from 'src/models/Tableau';
import { TableauxService } from '../tableaux.service';
import { ProjetService } from '../projet.service';
import { Projet } from 'src/models/Projet';
import { Carte } from 'src/models/Carte';

interface ProjetDashboard {
  projet: Projet;
  cartes: Carte[];
  cartesAFaire: number;
  cartesEnCours: number;
  cartesTermine: number;
  totalCartes: number;
  progressPercent: number;
  expanded: boolean;
}

@Component({
  selector: 'app-front',
  templateUrl: './front.component.html',
  styleUrls: ['./front.component.css']
})
export class FrontComponent implements OnInit {
  status = false;
  profileImage: string | undefined;
  users: Utilisateur[] = [];
  tableaux: Tableau[] = [];
  isAdmin: boolean = false;
  currentUser: Utilisateur | null = null;
  currentEmail: string = '';
  displayTableForm: boolean = false;
  userForm: Utilisateur = {} as Utilisateur;
  showUpdateForm: boolean = false;
  tableForm: Tableau = { nom: '', description: '', proprietaire: null, projets: [] };

  projetsDashboard: ProjetDashboard[] = [];
  loading = true;

  
  totalProjets = 0;
  totalCartes = 0;
  totalAFaire = 0;
  totalEnCours = 0;
  totalTermine = 0;
  globalProgress = 0;
  totalMembres = 0;

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
    this.loadDashboard();
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
    const currentUserString = localStorage.getItem('currentUser');
    if (currentUserString) {
      try {
        const stored = JSON.parse(currentUserString);
        this.currentUser = { ...stored, email: this.currentEmail || stored.email };
        if (this.currentUser && this.currentUser.image) {
          this.profileImage = this.getImageUrl(this.currentUser.image);
        }
        if (this.currentUser) {
          this.userForm.firstname = this.currentUser.firstname || '';
          this.userForm.email = this.currentUser.email || '';
        }
      } catch (error) {
        console.error('Erreur localStorage:', error);
      }
    }
  }

 
  loadDashboard(): void {
    this.loading = true;
    this.projetsDashboard = [];

    this.projetService.getAllProjects().subscribe(
      (projets: Projet[]) => {
        // Filtrer selon le rôle
        let filteredProjets = projets;
        if (!this.isAdmin && this.currentEmail) {
          filteredProjets = projets.filter(p =>
            p.utilisateurs &&
            p.utilisateurs.some((u: any) =>
              u.email?.toLowerCase() === this.currentEmail.toLowerCase()
            )
          );
        }

        this.totalProjets = filteredProjets.length;

        if (filteredProjets.length === 0) {
          this.loading = false;
          return;
        }

        // Collecter tous les membres uniques
        const allEmails = new Set<string>();

        let loadedCount = 0;
        filteredProjets.forEach(projet => {
          // Compter les membres
          if (projet.utilisateurs) {
            projet.utilisateurs.forEach((u: any) => {
              if (u.email) allEmails.add(u.email.toLowerCase());
            });
          }

          if (projet.idProjet) {
            this.projetService.getCartesDuProjet(projet.idProjet).subscribe(
              (cartes: Carte[]) => {
                const aFaire = cartes.filter(c => !c.statut || c.statut === 'A_FAIRE').length;
                const enCours = cartes.filter(c => c.statut === 'EN_COURS').length;
                const termine = cartes.filter(c => c.statut === 'TERMINE').length;
                const total = cartes.length;
                const progress = total > 0 ? Math.round((termine / total) * 100) : 0;

                this.projetsDashboard.push({
                  projet,
                  cartes,
                  cartesAFaire: aFaire,
                  cartesEnCours: enCours,
                  cartesTermine: termine,
                  totalCartes: total,
                  progressPercent: progress,
                  expanded: false
                });

                loadedCount++;
                if (loadedCount === filteredProjets.length) {
                  this.totalMembres = allEmails.size;
                  this.calculateTotals();
                  this.loading = false;
                }
              },
              () => {
                this.projetsDashboard.push({
                  projet, cartes: [],
                  cartesAFaire: 0, cartesEnCours: 0, cartesTermine: 0,
                  totalCartes: 0, progressPercent: 0, expanded: false
                });
                loadedCount++;
                if (loadedCount === filteredProjets.length) {
                  this.totalMembres = allEmails.size;
                  this.calculateTotals();
                  this.loading = false;
                }
              }
            );
          } else {
            loadedCount++;
          }
        });
      },
      () => { this.loading = false; }
    );
  }

  private calculateTotals(): void {
    this.totalCartes = this.projetsDashboard.reduce((s, p) => s + p.totalCartes, 0);
    this.totalAFaire = this.projetsDashboard.reduce((s, p) => s + p.cartesAFaire, 0);
    this.totalEnCours = this.projetsDashboard.reduce((s, p) => s + p.cartesEnCours, 0);
    this.totalTermine = this.projetsDashboard.reduce((s, p) => s + p.cartesTermine, 0);
    this.globalProgress = this.totalCartes > 0
      ? Math.round((this.totalTermine / this.totalCartes) * 100) : 0;
  }

  toggleProject(pd: ProjetDashboard): void {
    pd.expanded = !pd.expanded;
  }

  getStatutLabel(statut: string | undefined): string {
    switch (statut) {
      case 'EN_COURS': return 'In Progress';
      case 'TERMINE': return 'Done';
      default: return 'To Do';
    }
  }

  getStatutClass(statut: string | undefined): string {
    switch (statut) {
      case 'EN_COURS': return 'badge-orange';
      case 'TERMINE': return 'badge-green';
      default: return 'badge-red';
    }
  }

  // ─── Common methods ───
  toggleUpdateForm(): void { this.showUpdateForm = !this.showUpdateForm; }

  private checkUserRole(): void {
    this.isAdminService.getIsAdmin().subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });
    const currentUserString = localStorage.getItem('currentUser');
    if (currentUserString) {
      const currentUser = JSON.parse(currentUserString);
      if (currentUser && currentUser.image) {
        this.profileImage = this.getImageUrl(currentUser.image);
      }
    }
  }

  getImageUrl(filename: string): string {
    return `http://localhost:8086/api/v1/auth/get-image/${filename}`;
  }

  logout(): void {
    this.authService.logout();
    localStorage.removeItem('access_token');
    localStorage.removeItem('currentUser');
    this.router.navigate(['/login']);
  }

  addToggle(): void { this.status = !this.status; }

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
}