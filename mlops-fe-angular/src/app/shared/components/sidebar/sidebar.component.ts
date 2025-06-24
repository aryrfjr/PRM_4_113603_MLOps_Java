// NOTE: This reusable component is declared in AppModule (app.module.ts)

import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'side-bar', // defines the reusable <side-bar> tag
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})

export class SidebarComponent {

  constructor(private authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();   // Clear token
    this.router.navigate(['/login']); // Redirect to login screen
  }

}
