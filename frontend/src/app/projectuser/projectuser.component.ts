import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { IsAdminService } from '../shared/is-admin.service';
import { FileService } from '../file.service';
import { UserService } from '../user.service';
import { TableauxService } from '../tableaux.service';
import { Utilisateur } from 'src/models/Utilisateur';
import { Tableau } from 'src/models/Tableau';
import { Projet } from 'src/models/Projet';
import { ProjetService } from '../projet.service';

@Component({
  selector: 'app-projectuser',
  templateUrl: './projectuser.component.html',
  styleUrls: ['./projectuser.component.css'],
})
export class ProjectuserComponent implements OnInit {
  status = false;
  currentUser: Utilisateur | null = null;
  profileImage: string | undefined;
  userForm: Utilisateur = {} as Utilisateur;
  showUpdateForm: boolean = false;
  isAdmin: boolean = false;
  displayTableForm: boolean = false;
  usersEmails: string[] = [];
  membersList: any[] = []; // Déclarer membersList pour contenir la liste des membres
  showMembersList: boolean = false;
  projet: Projet | undefined;
  projet1: Projet | undefined;

  projectId: number | undefined;
  showUserEmails = false;
  selectedProjectId?: number;
  projects: Projet[] = [];
  allemail?: (string | undefined)[];
  tableForm: Tableau = {
    nom: '',
    description: '',
    proprietaire: null,
    projets: [],
  };
  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private isAdminService: IsAdminService,
    private fileService: FileService,
    private userService: UserService,
    private tableauService: TableauxService,
    private projetService: ProjetService,
  ) {}
  ngOnInit(): void {
    this.loadUserData();
    this.checkUserRole();
    this.loadProjects();
  }
  loadUserData(): void {
    const currentUserString = localStorage.getItem('currentUser');
    if (currentUserString) {
      try {
        this.currentUser = JSON.parse(currentUserString);
        if (this.currentUser && this.currentUser.image) {
          this.profileImage = this.getImageUrl(this.currentUser.image);
        }
        // Pré-remplir le formulaire avec les informations actuelles de l'utilisateur
        if (this.currentUser) {
          this.userForm.firstname = this.currentUser.firstname || '';
          this.userForm.email = this.currentUser.email || '';
          // Ajoutez d'autres champs si nécessaire
        }
      } catch (error) {
        console.error(
          "Erreur lors de la lecture des données de l'utilisateur depuis le localStorage :",
          error,
        );
      }
    } else {
      console.error('Aucun utilisateur connecté trouvé dans le localStorage.');
    }
  }
  getImageUrl(filename: string): string {
    return `http://localhost:8086/api/v1/auth/get-image/${filename}`;
  }
  onSubmit(): void {
    if (this.currentUser && this.currentUser.id) {
      const userId = this.currentUser.id;
      this.userService
        .updateUser(userId, this.userForm as Utilisateur)
        .subscribe(
          (updatedUser) => {
            console.log('Utilisateur mis à jour avec succès :', updatedUser);
            // Mettez à jour les données de l'utilisateur dans votre composant si nécessaire
            if (updatedUser) {
              this.currentUser = updatedUser;
              // Mettez à jour le formulaire avec les nouvelles données
              this.userForm.firstname = updatedUser.firstname || '';
              this.userForm.email = updatedUser.email || '';
              // Ajoutez d'autres champs si nécessaire

              // Mettre à jour les données de l'utilisateur dans localStorage
              localStorage.setItem('currentUser', JSON.stringify(updatedUser));
            }
            // Implémentez la logique de gestion de la réponse ici (par exemple, affichez un message de succès)
          },
          (error) => {
            console.error(
              "Erreur lors de la mise à jour de l'utilisateur :",
              error,
            );
            // Gérez les erreurs ici (par exemple, affichez un message d'erreur à l'utilisateur)
          },
        );
    } else {
      console.error('ID utilisateur non défini.');
      // Gérer le cas où currentUser ou currentUser.id n'est pas défini
    }
  }
  toggleUpdateForm(): void {
    this.showUpdateForm = !this.showUpdateForm;
  }
  private checkUserRole(): void {
    this.isAdminService.getIsAdmin().subscribe((isAdmin) => {
      this.isAdmin = isAdmin;
      console.log('isAdmin in front component:', this.isAdmin);
    });
    const currentUserString = localStorage.getItem('currentUser');
    if (currentUserString) {
      const currentUser = JSON.parse(currentUserString);

      // Utilisez les informations de l'utilisateur
      if (currentUser && currentUser.image) {
        this.profileImage = this.getImageUrl(currentUser.image);
      }
    } else {
      console.error('Aucun utilisateur connecté trouvé dans le localStorage.');
      // Gérer le cas où aucune information d'utilisateur n'est trouvée
    }
  }
  logout(): void {
    this.authService.logout();

    localStorage.removeItem('access_token');

    this.router.navigate(['/login']);
  }
  addToggle() {
    this.status = !this.status;
  }
  showTableForm(): void {
    this.displayTableForm = true;
  }

  onSubmitTableau(): void {
    if (this.currentUser && this.currentUser.id) {
      this.tableauService
        .createTableau(this.currentUser.id, this.tableForm)
        .subscribe(
          (newTableau: Tableau) => {
            console.log('Nouveau tableau créé avec succès :', newTableau);
            this.tableForm = {
              nom: '',
              description: '',
              proprietaire: null,
              projets: [],
            };
            this.displayTableForm = false;
          },
          (error: any) => {
            console.error('Erreur lors de la création du tableau :', error);
            // Gérer les erreurs ici
          },
        );
    } else {
      console.error('ID utilisateur non défini.');
      // Gérer le cas où currentUser ou currentUser.id n'est pas défini
    }
  }
  toggleTableForm(): void {
    this.displayTableForm = !this.displayTableForm;
  }
  loadProjects(): void {
  this.projetService.getAllProjects().subscribe(
    (projects: Projet[]) => {
      // Si ADMIN → afficher tous les projets
      // Si MEMBRE → afficher seulement les projets où il est membre
      if (this.isAdmin) {
        this.projects = projects.map(project => ({
          ...project,
          membersList: []
        }));
      } else {
        // Extraire l'email depuis le JWT
        const token = localStorage.getItem('access_token');
        let currentEmail = '';
        if (token) {
          try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            currentEmail = JSON.parse(atob(base64)).sub || '';
          } catch (e) {}
        }

        // Filtrer : garder seulement les projets où l'email est dans les utilisateurs
        this.projects = projects
          .filter(project =>
            project.utilisateurs &&
            project.utilisateurs.some((user: any) =>
              user.email?.toLowerCase() === currentEmail.toLowerCase()
            )
          )
          .map(project => ({
            ...project,
            membersList: []
          }));
      }
      console.log('Projects loaded:', this.projects);
    },
    (error) => {
      console.error('Error loading projects:', error);
    }
  );
}
  fetchUserEmails(idProjet: number): void {
    console.log('Fetching emails for project with ID:', idProjet);
    this.selectedProjectId = idProjet;

    // Utiliser getAllUser() au lieu de getAllUserEmails()
    this.userService.getAllUser().subscribe(
      (users: any[]) => {
        const allEmails = users.map((u) => u.email);
        console.log('All emails:', allEmails);

        // Filtrer : exclure ceux déjà dans le projet
        const projet = this.projects.find((p) => p.idProjet === idProjet);
        if (projet) {
          this.usersEmails = allEmails.filter(
            (email) =>
              !projet.utilisateurs.some((user: any) => user.email === email),
          );
          this.showUserEmails = true;
          console.log('Filtered emails:', this.usersEmails);
        }
      },
      (error) => {
        console.error('Erreur récupération utilisateurs:', error);
      },
    );
  }
  ajouterMembreAuProjet(email: string, idProjet?: number): void {
    if (idProjet !== undefined) {
      this.projetService
        .ajouterUtilisateurAuProjetParEmail(email, idProjet)
        .subscribe(
          (response) => {
            console.log(
              'Utilisateur ajouté au projet avec succès : ',
              response,
            );

            // Mettre à jour le projet dans la liste this.projects
            const projectToUpdate = this.projects.find(
              (project) => project.idProjet === idProjet,
            );
            if (projectToUpdate) {
              // Mettre à jour la liste des utilisateurs associés au projet
              this.projetService.getProjetById(idProjet).subscribe(
                (updatedProject) => {
                  console.log('Projet mis à jour : ', updatedProject);
                  projectToUpdate.utilisateurs = updatedProject.utilisateurs;
                  // Mettre à jour le nombre de membres
                  projectToUpdate.nombreMembres =
                    updatedProject.utilisateurs.length;
                },
                (error) => {
                  console.error(
                    'Erreur lors de la récupération du projet mis à jour : ',
                    error,
                  );
                },
              );
            } else {
              console.error(
                'Projet non trouvé pour mettre à jour le nombre de membres.',
              );
            }
          },
          (error) => {
            console.error(
              "Erreur lors de l'ajout de l'utilisateur au projet : ",
              error,
            );
            // Gérer les erreurs ici
          },
        );
    } else {
      console.error('Erreur : ID du projet non défini.');
      // Gérer le cas où idProjet est undefined, par exemple, afficher un message d'erreur à l'utilisateur ou prendre une autre action appropriée.
    }
  }

  getProjetById(): void {
    if (this.projectId !== undefined) {
      this.projetService.getProjetById(this.projectId).subscribe(
        (data) => {
          this.projet = data;
          console.log('Projet récupéré:', this.projet);
        },
        (error) => {
          console.error('Erreur lors de la récupération du projet:', error);
          // Gérer l'erreur selon les besoins de votre application
        },
      );
    } else {
      console.error('Erreur : ID du projet non défini.');
      // Gérer le cas où this.projectId est undefined, par exemple, afficher un message d'erreur à l'utilisateur ou prendre une autre action appropriée.
    }
  }

  isUserInAnyProject(email: string): boolean {
    // Logique pour vérifier si l'utilisateur avec l'email donné est déjà associé à un projet
    return this.projects.some((project) =>
      project.utilisateurs.some((user) => user.email === email),
    );
  }

  toggleDescription(project: Projet): void {
    project.showFullDescription = !project.showFullDescription;
  } // projectuser.component.ts
  voirMembresDuProjet(idProjet?: number): void {
    if (idProjet !== undefined) {
      this.projetService.getProjetById(idProjet).subscribe(
        (projet) => {
          console.log('Membres du projet:', projet.utilisateurs);
          this.projet1 = projet;
          console.log(this.projects);
          projet.utilisateurs.forEach((e) => {
            console.log('email', e.email);
          });
          // Mettre à jour la liste des membres associés
          this.membersList = projet.utilisateurs;
          const emails = projet.utilisateurs.map((user) => user.email);
          this.allemail = emails;
          console.log('all emails ', this.allemail);
          console.log('emails ', emails);
          console.log('email', this.membersList);

          this.showMembersList = true; // Afficher la liste des membres
        },
        (error) => {
          console.error(
            'Erreur lors de la récupération des membres du projet : ',
            error,
          );
        },
      );
    } else {
      console.error('Erreur : ID du projet non défini.');
    }
  }

  // Autres méthodes de votre composant
}
