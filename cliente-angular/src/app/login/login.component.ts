import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GraphqlService } from '../graphql.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = 'usuario1';
  password = '1234';
  error = '';
  loading = false;

  constructor(private graphql: GraphqlService, private router: Router) {}

  ingresar(): void {
    this.error = '';
    this.loading = true;
    this.graphql.login(this.username, this.password).subscribe({
      next: auth => {
        this.loading = false;
        if (!auth.authenticated) {
          this.error = auth.message || 'Credenciales inválidas';
          return;
        }
        localStorage.setItem('token', auth.token);
        localStorage.setItem('userId', auth.user.id);
        this.router.navigateByUrl('/panel');
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo conectar con el Gateway GraphQL';
      }
    });
  }
}
