import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls:['./sidebar.component.css']
})
export class SidebarComponent {
  clientId: string | null = null;

  constructor(private router: Router) {
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => {
        const match = this.router.url.match(/clients\/([^/]+)/);
        this.clientId = match ? match[1] : null;
      });
  }
  get isCreatingClient(): boolean {
    return this.router.url.includes('/clients/create');
  }
}
