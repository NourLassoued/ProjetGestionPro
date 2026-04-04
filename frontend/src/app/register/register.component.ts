import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Role } from 'src/models/Role';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import {MatSnackBar} from "@angular/material/snack-bar";
import { FileService } from '../file.service';
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent  implements OnInit{
  selectedRole: Role = Role.ADMINISTRATEUR; // Initialisez la propriété avec une valeur par défaut si nécessaire

  registerForm!: FormGroup;

  selectedFile!: File;
  constructor(private authService: AuthService, private fb: FormBuilder, private router: Router, private snackbar: MatSnackBar, private fileService: FileService) {
  }
  ngOnInit(): void {
  
    this.registerForm = this.fb.group({
      firstname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email, Validators.pattern("^.*@gmail.com$")]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      image: [''],
    
      agreedToTerms: [false, Validators.requiredTrue]
    });
  } onSubmit(): void {
    if (this.registerForm.valid) {
      if (this.selectedFile) {
        this.fileService.uploadFile(this.selectedFile).subscribe(
          (response: any) => {
            const filename = response.split(': ')[1];
            console.log(filename);
            this.registerForm.patchValue({ image: filename });
            this.register();
          },
          error => {
            console.error('Error uploading file:', error);
            // Handle error uploading file (e.g., show error message)
          }
        );
      } else {
        this.register();
      }
    } else {
      console.error('Invalid form');
      // Handle invalid form (e.g., show error message to user)
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
    }
  }

  register(): void {
    const formData = { ...this.registerForm.value };

    this.authService.register(formData).subscribe(
      (response: any) => {
        this.registerForm.reset();
        console.log("response : ", response);
        this.snackbar.open('User added successfully!', 'Close', {
          duration: 9000,
        }).afterDismissed().subscribe(() => {
          this.router.navigate(['/login']);
        });
      },
      error => {
        console.error('Error registering user:', error);
        this.snackbar.open('Error while registering user. Please try again.', 'Close', {
          duration: 5000,
        });
      }
    );
  }
}