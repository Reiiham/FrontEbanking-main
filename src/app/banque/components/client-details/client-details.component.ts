import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ClientService } from '../../../services/banque.service';
import { ServiceManagerService } from '../../../services/service-manager.service';
import { ClientSummaryDTO } from '../../models/client-summary.model';

@Component({
  selector: 'app-client-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-details.component.html',
  styleUrls: ['./client-details.component.css']
})
export class ClientDetailsComponent implements OnInit {
  client?: ClientSummaryDTO;
  clientId: string = '';
  message: string = '';
  error: string = '';
  isLoading: boolean = false;

  activeTab: 'activer' | 'suspendre' | 'reactiver' = 'activer';

  availableServices = [
    'VIREMENT', 'PAIEMENTS_EN_LIGNE', 'CONSULTATION_SOLDE',
    'HISTORIQUE_TRANSACTIONS', 'CHEQUIER', 'BLOQUER_CARTE',
    'PRET_PERSONNEL', 'EPARGNE', 'INVESTISSEMENT',
    'ALERTES', 'FACTURES', 'MULTIDEVISES',
    'SERVICE_CLIENT', 'MESSAGERIE_SECURISÉE'
  ];

  newServices: string[] = [];
  suspendedServices: string[] = [];
  reactivatedServices: string[] = [];

  suspensionReason: string = '';
  suspensionMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private clientService: ClientService,
    private serviceManager: ServiceManagerService
  ) {
    // 🔍 Debug: Component constructor called
    console.log('🔍 ClientDetailsComponent constructor called');
  }

  ngOnInit(): void {
    console.log('🔍 ClientDetailsComponent ngOnInit started');

    // Check if token still exists
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    console.log('🔍 Token in ClientDetails ngOnInit:', token ? 'EXISTS' : 'MISSING');

    const id = this.route.snapshot.paramMap.get('id');
    console.log('🔍 Route param ID:', id);

    if (id) {
      this.clientId = id;
      this.isLoading = true;

      console.log('🔍 About to call clientService.getClientById');

      this.clientService.getClientById(id).subscribe({
        next: data => {
          console.log('✅ getClientById SUCCESS:', data);
          this.client = data;
        },
        error: (error) => {
          console.error('❌ getClientById ERROR:', error);
          console.error('❌ Error status:', error.status);
          console.error('❌ Error message:', error.message);

          // Check if it's an auth error
          if (error.status === 401 || error.status === 403) {
            console.error('🚨 AUTHENTICATION ERROR - This might trigger logout');
          }

          this.error = "Erreur lors du chargement du client.";
        },
        complete: () => {
          console.log('🔍 getClientById completed');
          this.isLoading = false;
        }
      });
    } else {
      console.error('❌ No client ID found in route params');
    }
  }

  // get isEligible(): boolean {
  //   return this.client !== undefined &&
  //     !this.client['compteBloque'] &&
  //     this.client['documentsComplets'];
  // }

  // get isEligible(): boolean {
  //   console.log('=== DEBUG isEligible ===');
  //   console.log('Client:', this.client);
  //   console.log('compteBloque:', this.client?.compteBloque, 'Type:', typeof this.client?.compteBloque);
  //   console.log('documentsComplets:', this.client?.documentsComplets, 'Type:', typeof this.client?.documentsComplets);
  //
  //   if (!this.client) {
  //     console.log('❌ Client undefined');
  //     return false;
  //   }
  //
  //   const isNotBlocked = this.client.compteBloque === false;
  //   const hasCompleteDocuments = this.client.documentsComplets === true;
  //
  //   console.log('isNotBlocked:', isNotBlocked);
  //   console.log('hasCompleteDocuments:', hasCompleteDocuments);
  //   console.log('Final result:', isNotBlocked && hasCompleteDocuments);
  //
  //   return isNotBlocked && hasCompleteDocuments;
  // }

  get isEligible(): boolean {
    if (!this.client) return false;

    // Si null, on considère comme "autorisé"
    const isNotBlocked = (this.client.compteBloque === null || this.client.compteBloque === false);
    const hasCompleteDocuments = (this.client.documentsComplets === null || this.client.documentsComplets === true);

    return isNotBlocked && hasCompleteDocuments;
  }


  toggleService(service: string, list: string[]): void {
    const index = list.indexOf(service);
    if (index > -1) list.splice(index, 1);
    else list.push(service);
  }

  activate(): void {
    console.log('🚨 ACTIVATE METHOD CALLED');
    this.clearMessages();
    console.log('🔍 Activate method called');
    console.log('🔍 Client data:', this.client);
    console.log('🔍 Client ID:', this.clientId);

    // 🔁 Recharger les données du client AVANT de valider l'éligibilité
    this.clientService.getClientById(this.clientId).subscribe({
      next: (client) => {
        console.log('🔄 Client reloaded:', client);
        this.client = client;

        console.log('🔍 About to check isEligible...');
        console.log('🔍 isEligible value:', this.isEligible);

        if (!this.isEligible) {
          console.log('❌ NOT ELIGIBLE - Setting error message');
          this.error = "Le client est bloqué ou n'a pas tous les documents complets.";
          return;
        }

        console.log('✅ Client is eligible, checking services...');

        if (this.newServices.length === 0) {
          console.log('❌ No services selected');
          this.error = "Veuillez sélectionner des services à activer.";
          return;
        }

        console.log('🔍 Services to activate:', this.newServices);

        const request = {
          clientId: this.clientId,
          services: this.newServices
        };

        this.isLoading = true;
        this.serviceManager.activateServices(request).subscribe({
          next: () => {
            console.log('✅ Services activated successfully');
            this.message = "Services activés avec succès.";
          },
          error: (error) => {
            console.error('❌ activateServices ERROR:', error);
            this.error = "Erreur lors de l'activation.";
          },
          complete: () => this.isLoading = false
        });
      },
      error: (error) => {
        console.error('❌ Reload client ERROR:', error);
        this.error = "Impossible de vérifier les statuts du client.";
      }
    });
  }

  // activate(): void {
  //   this.clearMessages();
  //   console.log('🔍 Activate method called');
  //   console.log(this.client);
  //
  //   // 🔁 Recharger les données du client AVANT de valider l'éligibilité
  //   this.clientService.getClientById(this.clientId).subscribe({
  //     next: (client) => {
  //       this.client = client;
  //
  //       if (!this.isEligible) {
  //         this.error = "Le client est bloqué ou n'a pas tous les documents complets.";
  //         return;
  //       }
  //
  //       if (this.newServices.length === 0) {
  //         this.error = "Veuillez sélectionner des services à activer.";
  //         return;
  //       }
  //
  //       const request = {
  //         clientId: this.clientId,
  //         services: this.newServices
  //       };
  //
  //       this.isLoading = true;
  //       this.serviceManager.activateServices(request).subscribe({
  //         next: () => this.message = "Services activés avec succès.",
  //         error: (error) => {
  //           console.error('❌ activateServices ERROR:', error);
  //           this.error = "Erreur lors de l'activation.";
  //         },
  //         complete: () => this.isLoading = false
  //       });
  //     },
  //     error: (error) => {
  //       console.error('❌ Reload client ERROR:', error);
  //       this.error = "Impossible de vérifier les statuts du client.";
  //     }
  //   });
  // }

  suspend(): void {
    this.clearMessages();
    if (this.suspendedServices.length === 0) {
      this.error = "Veuillez sélectionner des services à suspendre.";
      return;
    }

    const request = {
      servicesToSuspend: this.suspendedServices,
      reason: this.suspensionReason,
      notificationMessage: this.suspensionMessage
    };

    this.isLoading = true;
    this.serviceManager.suspendServices(this.clientId, request).subscribe({
      next: () => this.message = "Services suspendus avec succès.",
      error: (error) => {
        console.error('❌ suspendServices ERROR:', error);
        this.error = "Erreur lors de la suspension.";
      },
      complete: () => this.isLoading = false
    });
  }

  reactivate(): void {
    this.clearMessages();
    if (this.reactivatedServices.length === 0) {
      this.error = "Veuillez sélectionner des services à réactiver.";
      return;
    }

    this.isLoading = true;
    this.serviceManager.reactivateServices(this.clientId, this.reactivatedServices).subscribe({
      next: () => this.message = "Services réactivés avec succès.",
      error: (error) => {
        console.error('❌ reactivateServices ERROR:', error);
        this.error = "Erreur lors de la réactivation.";
      },
      complete: () => this.isLoading = false
    });
  }

  private clearMessages(): void {
    this.message = '';
    this.error = '';
  }
}
