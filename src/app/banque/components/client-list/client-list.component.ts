import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ClientService } from '../../../services/banque.service';
import { ClientSummaryDTO } from '../../models/client-summary.model';

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {
  clients: ClientSummaryDTO[] = [];
  searchQuery: string = '';
  error = '';

  sortField: string = 'name'; // ou 'accounts'
  sortDirection: 'asc' | 'desc' = 'asc';


  constructor(private clientService: ClientService) {}

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
  this.clientService.getAllClients().subscribe({
    next: data => {
      this.clients = data;
      this.sortClients(); // 👈 Ajoute ceci
    },
    error: () => this.error = 'Erreur lors du chargement des clients.'
  });
}


  search(): void {
  if (this.searchQuery.trim()) {
    this.clientService.searchClientsByName(this.searchQuery).subscribe({
      next: data => {
        this.clients = data;
        this.sortClients(); // 👈 Ajoute ceci aussi
      },
      error: () => this.error = 'Erreur lors de la recherche.'
    });
  } else {
    this.loadClients();
  }
}


  sortClients(): void {
  this.clients.sort((a, b) => {
    let aVal: any;
    let bVal: any;

    switch (this.sortField) {
      case 'name':
        aVal = a.fullName.toLowerCase();
        bVal = b.fullName.toLowerCase();
        break;
      case 'accounts':
        aVal = a.accounts?.length || 0;
        bVal = b.accounts?.length || 0;
        break;
      case 'balance':
        aVal = a.accounts?.reduce((sum, acc) => sum + acc.balance, 0) || 0;
        bVal = b.accounts?.reduce((sum, acc) => sum + acc.balance, 0) || 0;
        break;
      default:
        return 0;
    }

    if (aVal < bVal) return this.sortDirection === 'asc' ? -1 : 1;
    if (aVal > bVal) return this.sortDirection === 'asc' ? 1 : -1;
    return 0;
  });
}

getTotalBalance(client: ClientSummaryDTO): number {
  return client.accounts?.reduce((total, acc) => total + acc.balance, 0) || 0;
}



}
