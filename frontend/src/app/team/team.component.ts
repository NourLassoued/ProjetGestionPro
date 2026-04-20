import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { IsAdminService } from '../shared/is-admin.service';
import { UserService } from '../user.service';
import { ProjetService } from '../projet.service';
import { Utilisateur } from 'src/models/Utilisateur';
import { Projet } from 'src/models/Projet';
import { Message } from 'src/models/Message';
import { MessageService } from '../message.service';

@Component({
  selector: 'app-team',
  templateUrl: './team.component.html',
  styleUrls: ['./team.component.css']
})
export class TeamComponent implements OnInit, OnDestroy {

  @ViewChild('chatBody') chatBody!: ElementRef;

  constructor(
    private authService: AuthService,
    private router: Router,
    private isAdminService: IsAdminService,
    private userService: UserService,
    private projetService: ProjetService,
    private messageService: MessageService
  ) {}

  status = false;
  isAdmin = false;
  profileImage: string | undefined;
  currentEmail = '';
  showUpdateForm = false;
  userForm: Utilisateur = {} as Utilisateur;
  currentUser: Utilisateur | null = null;

  // Projects & Chat
  projetsUtilisateur: Projet[] = [];
  projetSelectionne: Projet | null = null;
  messages: Message[] = [];
  newMessage = '';
  pollingInterval: any = null;
  loading = false;

  ngOnInit(): void {
    this.checkUserRole();
    this.loadUserData();
  }

  ngOnDestroy(): void {
    this.stopPolling();
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
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        this.currentUser = { ...parsed, email: this.currentEmail };
        if (this.currentUser?.image) {
          this.profileImage = this.getImageUrl(this.currentUser.image);
        }
        this.userForm.firstname = this.currentUser?.firstname || '';
        this.userForm.email = this.currentUser?.email || '';
      } catch (e) {}
    }
    this.loadProjets();
  }

  private checkUserRole(): void {
    this.isAdminService.getIsAdmin().subscribe(isAdmin => {
      this.isAdmin = isAdmin;
    });
  }

  loadProjets(): void {
    this.projetService.getAllProjects().subscribe((projets: Projet[]) => {
      if (this.isAdmin) {
        this.projetsUtilisateur = projets;
      } else {
        this.projetsUtilisateur = projets.filter(p =>
          p.utilisateurs?.some((u: any) =>
            u.email?.toLowerCase() === this.currentEmail.toLowerCase()
          )
        );
      }
    });
  }

  selectProject(projet: Projet): void {
    this.projetSelectionne = projet;
    this.messages = [];
    this.stopPolling();
    if (projet.idProjet) {
      this.loadMessages(projet.idProjet);
      this.startPolling(projet.idProjet);
    }
  }

  loadMessages(idProjet: number): void {
    this.loading = true;
    this.messageService.getMessages(idProjet).subscribe(
      (msgs) => {
        this.messages = msgs;
        this.loading = false;
        setTimeout(() => this.scrollToBottom(), 100);
      },
      () => { this.loading = false; }
    );
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.projetSelectionne?.idProjet) return;

    this.messageService.envoyerMessage(this.projetSelectionne.idProjet, this.newMessage.trim()).subscribe(
      (msg) => {
        this.messages.push(msg);
        this.newMessage = '';
        setTimeout(() => this.scrollToBottom(), 100);
      }
    );
  }

  // Polling toutes les 3 secondes pour simuler le temps réel
  startPolling(idProjet: number): void {
    this.pollingInterval = setInterval(() => {
      this.messageService.getMessages(idProjet).subscribe((msgs) => {
        if (msgs.length !== this.messages.length) {
          this.messages = msgs;
          setTimeout(() => this.scrollToBottom(), 100);
        }
      });
    }, 3000);
  }

  stopPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  scrollToBottom(): void {
    if (this.chatBody) {
      this.chatBody.nativeElement.scrollTop = this.chatBody.nativeElement.scrollHeight;
    }
  }

  isMyMessage(msg: Message): boolean {
    return msg.expediteur?.email?.toLowerCase() === this.currentEmail.toLowerCase();
  }

  getImageUrl(filename: string): string {
    return `http://localhost:8086/api/v1/auth/get-image/${filename}`;
  }

  addToggle(): void { this.status = !this.status; }
  toggleUpdateForm(): void { this.showUpdateForm = !this.showUpdateForm; }

  logout(): void {
    this.authService.logout();
    localStorage.removeItem('access_token');
    localStorage.removeItem('currentUser');
    this.router.navigate(['/login']);
  }

  onSubmit(): void {
    if (this.currentUser && this.currentUser.id) {
      this.userService.updateUser(this.currentUser.id, this.userForm as Utilisateur).subscribe(
        (u) => {
          if (u) {
            this.currentUser = u;
            this.userForm.firstname = u.firstname || '';
            this.userForm.email = u.email || '';
            localStorage.setItem('currentUser', JSON.stringify(u));
          }
        }
      );
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}