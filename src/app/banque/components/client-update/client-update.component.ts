import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientService } from '../../../services/banque.service';
import { ClientSummaryDTO } from '../../models/client-summary.model';
import { ClientUpdateRequest } from '../../models/client-update-request.model';

@Component({
  selector: 'app-client-update',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-update.component.html',
  styleUrls:['./client-update.component.css']
})
export class ClientUpdateComponent implements OnInit {
  client?: ClientSummaryDTO;

  updatedFirstName = '';
  updatedLastName = '';
  updatedEmail = '';
  updatedPhone = '';
  supervisorPassword = '';
  updatedDocumentsComplets: boolean = false;
  updatedCompteBloque: boolean = false;

  message = '';
  error = '';

  constructor(
    private route: ActivatedRoute,
    private clientService: ClientService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.clientService.getClientById(id).subscribe({
        next: data => {
          this.client = data;

          // Split full name (assumes "First Last")
          const parts = data.fullName.split(' ');
          this.updatedFirstName = parts[0];
          this.updatedLastName = parts.slice(1).join(' ');

          this.updatedEmail = (data as any).email ?? '';
          this.updatedPhone = (data as any).phone ?? '';
        },
        error: () => this.error = 'Erreur lors du chargement du client.'
      });
    }
  }

  update(): void {
  if (!this.client) return;


  // if (!this.authService.isLoggedIn()) {
  //   this.error = 'Session expirée, veuillez vous reconnecter';
  //   this.router.navigate(['/login']);
  //   return;
  // }

  const payload: ClientUpdateRequest = {
    clientId: this.client.clientId,
    newFirstName: this.updatedFirstName,
    newLastName: this.updatedLastName,
    newEmail: this.updatedEmail,
    newTel: this.updatedPhone,
    supervisorCode: this.supervisorPassword
  };

  console.log('🚀 Envoi de la mise à jour:', payload);
  
  this.clientService.updateClient(payload).subscribe({
    next: (response) => {
      console.log('✅ Réponse reçue:', response);
      this.message = 'Client mis à jour avec succès ✅';
      setTimeout(() => {
      this.router.navigate(['/employee/clients']);
      }, 1000);
    },
    error: (err) => {
      console.error('❌ Erreur complète:', err);
      console.error('❌ Status:', err.status);
      console.error('❌ Message:', err.message);
      this.error = 'Erreur de mise à jour ou mot de passe invalide ❌';
    }
  });
}
}
