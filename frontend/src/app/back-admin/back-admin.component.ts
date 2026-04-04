import { Component, Input, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../user.service';
import { Utilisateur } from 'src/models/Utilisateur';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Role } from 'src/models/Role';
import { IsAdminService } from '../shared/is-admin.service';
import { TableauxService } from '../tableaux.service';
import { Tableau } from 'src/models/Tableau';

@Component({
  selector: 'app-back-admin',
  templateUrl: './back-admin.component.html',
  styleUrls: ['./back-admin.component.css']
})
export class BackAdminComponent implements OnInit {
  @Input('appHidePassword') password: string = '';
  status = false;
  user: Utilisateur | undefined;
  displayTableForm: boolean = false;
  userForm: Utilisateur = {} as Utilisateur;
  tableForm: Tableau = { nom: '', description: '', proprietaire: null, projets: [] };
  currentUser: Utilisateur | null = null;
  id!: number;
  newRole!: string;
  profileImage: string | undefined;
  users: Utilisateur[] = [];
  isAdmin: boolean = false;
  showUpdateForm: boolean = false;
  roles: Role[] = [Role.ADMINISTRATEUR, Role.MEMBRE, Role.OBSERVATEUR];

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private isAdminService: IsAdminService,
    private tableauService: TableauxService
  ) {}

  ngOnInit(): void {
    this.getAllUsers();
    this.loadUserData();
    this.checkUserRole();
  }

  addToggle(): void {
    this.status = !this.status;
  }

  /** Compte les utilisateurs par rôle — utilisé dans le template */
  countByRole(role: string): number {
    return this.users.filter(u => u.role === role).length;
  }

  getAllUsers(): void {
    this.userService.getAllUser().subscribe(
      (data) => {
        this.users = data;
      },
      (error) => {
        console.log('Error fetching users:', error);
      }
    );
  }

  loadUserData(): void {
    const currentUserString = localStorage.getItem('currentUser');
    if (currentUserString) {
      try {
        this.currentUser = JSON.parse(currentUserString);
        if (this.currentUser && this.currentUser.image) {
          this.profileImage = this.getImageUrl(this.currentUser.image);
        }
        if (this.currentUser) {
          this.userForm.firstname = this.currentUser.firstname || '';
          this.userForm.email = this.currentUser.email || '';
        }
      } catch (error) {
        console.error('Erreur lecture localStorage :', error);
      }
    }
  }

  logout(): void {
    this.authService.logout();
    localStorage.removeItem('access_token');
    localStorage.removeItem('currentUser');
    this.router.navigate(['/login']);
  }

  deleteUser(user: Utilisateur): void {
    const snackBarRef = this.snackBar.open(
      `Are you sure you want to delete ${user.firstname} (${user.email})?`,
      'Confirm',
      { duration: 5000, horizontalPosition: 'center', verticalPosition: 'bottom' }
    );

    snackBarRef.onAction().subscribe(() => {
      if (user.id) {
        this.userService.deleteUser(user.id).subscribe(
          () => {
            this.users = this.users.filter(u => u !== user);
            this.snackBar.open(`User ${user.firstname} deleted successfully`, 'Close', {
              duration: 3000, horizontalPosition: 'center', verticalPosition: 'bottom'
            });
          },
          (error) => {
            console.error('Error deleting user:', error);
            this.snackBar.open('Error deleting user', 'Close', {
              duration: 3000, horizontalPosition: 'center', verticalPosition: 'bottom'
            });
          }
        );
      }
    });
  }

  getImageUrl(filename: string): string {
    return `http://localhost:8086/api/v1/auth/get-image/${filename}`;
  }

  onRoleChange(user: Utilisateur): void {
    if (user.id && user.role) {
      this.userService.changerRoleUtilisateur(user.id, user.role).subscribe(
        (data: Utilisateur) => {
          console.log('Rôle mis à jour :', data);
          this.snackBar.open(`Role updated for ${user.firstname}`, 'Close', {
            duration: 3000, horizontalPosition: 'center', verticalPosition: 'bottom'
          });
        },
        (error) => {
          console.error('Erreur mise à jour rôle :', error);
          this.snackBar.open('Error updating role', 'Close', {
            duration: 3000, horizontalPosition: 'center', verticalPosition: 'bottom'
          });
        }
      );
    }
  }

  showTableForm(): void {
    this.displayTableForm = true;
  }

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

  onSubmitTableau(): void {
    if (this.currentUser && this.currentUser.id) {
      this.tableauService.createTableau(this.currentUser.id, this.tableForm).subscribe(
        (newTableau: Tableau) => {
          console.log('Tableau créé :', newTableau);
          this.tableForm = { nom: '', description: '', proprietaire: null, projets: [] };
          this.displayTableForm = false;
        },
        (error: any) => {
          console.error('Erreur création tableau :', error);
        }
      );
    }
  }

  toggleTableForm(): void {
    this.displayTableForm = !this.displayTableForm;
  }

  onSubmit(): void {
    if (this.currentUser && this.currentUser.id) {
      const userId = this.currentUser.id;
      this.userService.updateUser(userId, this.userForm as Utilisateur).subscribe(
        (updatedUser) => {
          if (updatedUser) {
            this.currentUser = updatedUser;
            this.userForm.firstname = updatedUser.firstname || '';
            this.userForm.email = updatedUser.email || '';
            localStorage.setItem('currentUser', JSON.stringify(updatedUser));
          }
        },
        (error) => {
          console.error('Erreur mise à jour :', error);
        }
      );
    }
  }

  toggleUpdateForm(): void {
    this.showUpdateForm = !this.showUpdateForm;
  }
}