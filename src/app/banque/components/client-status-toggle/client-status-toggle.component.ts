import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientService } from '../../../services/banque.service';

@Component({
  selector: 'app-client-status-toggle',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-status-toggle.component.html',
  styleUrls:['./client-status-toggle.component.css']
})
export class ClientStatusToggleComponent implements OnInit {
  clientId: string = '';
  compteBloque: boolean = false;
  documentsComplets: boolean = true;
  message = '';
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clientService: ClientService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.clientId = id;
      // Optionnel : charger les valeurs actuelles depuis getClientById()
      this.clientService.getClientById(id).subscribe({
        next: data => {
          this.compteBloque = (data as any).compteBloque ?? false;
          this.documentsComplets = (data as any).documentsComplets ?? true;
        },
        error: () => this.error = "Erreur lors du chargement du client."
      });
    }
  }

  toggle(): void {
    this.message = '';
    this.error = '';


    console.log(">>> Mise à jour du statut :", this.compteBloque, this.documentsComplets);
    this.clientService.toggleClientStatus(this.clientId, this.compteBloque, this.documentsComplets).subscribe({
      next: () => this.message = "Statuts mis à jour avec succès",
      error: () => this.error = "Erreur lors de la mise à jour du statut"
    });
  }
}
