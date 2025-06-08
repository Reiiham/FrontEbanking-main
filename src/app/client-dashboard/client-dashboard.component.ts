import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ChatbotComponent} from '../components/chatbot/chatbot.component'; // Ajustez le chemin selon votre structure

interface BankAccount {
  id: string;
  rib: string;
  balance: number;
  type: string;
}

interface Beneficiary {
  id: number;
  nom: string;
  rib: string;
  relation: string;
  actif: boolean;
}

interface Transaction {
  id: string;
  type: string;
  amount: number;
  date: string;
  description?: string;
}

interface TransferRequest {
  fromAccountId: number;
  toRib: string;
  amount: number;
  description: string;
  beneficiaryName: string;
}


@Component({
  selector: 'app-client-dashboard',
  templateUrl:'client-dashboard.component.html',
  standalone:true,
  imports: [
    CommonModule,  // Provides *ngIf, *ngFor, currency pipe, date pipe, keyvalue pipe
    FormsModule,
    ChatbotComponent,
    // Provides ngModel, ngForm
  ],
  styleUrl:'client-dashboard.component.scss',
})
export class ClientDashboardComponent implements OnInit, OnDestroy {
  clientId: string | null = null;
  //activeTab: string = 'virement';
  private _activeTab: string = 'virement';

  private subscriptions: Subscription[] = [];

  // Données pour les virements
  clientAccounts: BankAccount[] = [];
  destinationType: string = 'manual'; // 'beneficiary' ou 'manual'
  selectedBeneficiaryId: string | null = null;  transferRequest: TransferRequest = {
    fromAccountId: 0,
    toRib: '',
    amount: 0,
    description: '',
    beneficiaryName: ''
  };
  ribValidation: any = null;

  // Données pour les bénéficiaires
  beneficiaries: Beneficiary[] = [];
  showAddBeneficiary: boolean = false;
  editingBeneficiary: Beneficiary | null = null;
  beneficiaryData: any = { nom: '', rib: '', relation: '' };
  relationTypes: any = {};
  activeFilter: string = '';
  searchQuery: string = '';

  // Données pour l'historique
  transferHistory: Transaction[] = [];
  historyPage: number = 0;
  historySize: number = 10;

  // Messages
  message: string = '';
  messageType: string = '';

  private apiUrl = 'http://localhost:8090/eBankingVer1_war_exploded/api/clients';
  private httpOptions: { headers: HttpHeaders } = { headers: new HttpHeaders() };

  constructor(private http: HttpClient, private authService: AuthService) {}

  private onTabChange(tab: string) {
    switch(tab) {
      case 'virement':
        if (this.clientId) {
          this.loadClientAccounts();
          this.loadBeneficiaries(); // Charger les bénéficiaires aussi
        }
        break;
      case 'beneficiaires':
        if (this.clientId) {
          this.loadBeneficiaries();
          this.loadRelationTypes(); // Charger automatiquement les types de relation
        }
        break;
      case 'historique':
        if (this.clientId) {
          this.loadTransferHistory();
        }
        break;
    }
  }
  ngOnInit() {
    console.log('Composant initialisé');

    // S'abonner aux changements du clientId
    const clientIdSub = this.authService.clientId$.subscribe(clientId => {
      console.log('ClientId reçu via observable:', clientId);
      this.clientId = clientId;
      if (clientId) {
        this.updateHttpOptions();
        this.initializeData();
      }
    });
    this.subscriptions.push(clientIdSub);

    // Si le clientId est déjà disponible, initialiser immédiatement
    const currentClientId = this.authService.getClientId();
    console.log('ClientId actuel:', currentClientId);

    if (currentClientId) {
      this.clientId = currentClientId;
      this.updateHttpOptions();
      this.initializeData();
    }
  }
  // Getter et setter pour activeTab
  get activeTab(): string {
    return this._activeTab;
  }
  parseCustomDate(dateValue: any): string {
    if (!dateValue) return '-';

    try {
      let parts: number[];

      // Handle different data types
      if (Array.isArray(dateValue)) {
        // It's already an array of numbers
        parts = dateValue;
      } else if (typeof dateValue === 'string') {
        // It's a comma-separated string
        parts = dateValue.split(',').map(part => parseInt(part.trim()));
      } else if (dateValue instanceof Date) {
        // It's already a Date object
        return dateValue.toLocaleDateString('fr-FR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
      } else {
        // Try to convert to string and then parse
        const dateString = String(dateValue);
        if (dateString.includes(',')) {
          parts = dateString.split(',').map(part => parseInt(part.trim()));
        } else {
          // Try to parse as regular date
          const date = new Date(dateString);
          if (!isNaN(date.getTime())) {
            return date.toLocaleDateString('fr-FR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            });
          }
          return dateString;
        }
      }

      // Ensure we have at least 6 parts [year, month, day, hour, minute, second]
      if (parts.length < 6) {
        return String(dateValue);
      }

      // Create Date object (month is 0-indexed in JavaScript)
      const date = new Date(
        parts[0], // year
        parts[1] - 1, // month (subtract 1 because JS months are 0-indexed)
        parts[2], // day
        parts[3], // hour
        parts[4], // minute
        parts[5], // second
        parts[6] ? Math.floor(parts[6] / 1000000) : 0 // nanoseconds to milliseconds (if present)
      );

      // Check if date is valid
      if (isNaN(date.getTime())) {
        return String(dateValue);
      }

      // Format the date nicely
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch (error) {
      console.error('Error parsing date:', error, 'Original value:', dateValue);
      return String(dateValue);
    }
  }
  set activeTab(value: string) {
    this._activeTab = value;
    // Charger automatiquement les données selon l'onglet
    this.onTabChange(value);
  }

  ngOnDestroy() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }


  private updateHttpOptions() {
    const token = this.authService.getToken();
    this.httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      })
    };
  }
  private initializeData() {
    console.log('Initialisation des données pour client:', this.clientId);

    // Charger selon l'onglet actif
    switch(this.activeTab) {
      case 'virement':
        this.loadClientAccounts();
        this.loadBeneficiaries();
        break;
      case 'beneficiaires':
        this.loadBeneficiaries();
        this.loadRelationTypes(); // Charger automatiquement
        break;
      case 'historique':
        this.loadTransferHistory();
        break;
      default:
        // Par défaut, charger les comptes
        this.loadClientAccounts();
        this.loadBeneficiaries();
        break;
    }
  }

  // Méthodes pour les virements

// Modifiez votre méthode loadClientAccounts pour gérer le XML
  loadClientAccounts() {
    if (!this.clientId) {
      console.warn('ClientId non disponible pour charger les comptes');
      return;
    }

    // Modifiez les headers pour accepter du XML
    const xmlHttpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/xml, application/json', // Accepter les deux
        'Authorization': this.authService.getToken() ? `Bearer ${this.authService.getToken()}` : ''
      }),
      responseType: 'text' as 'json' // Traiter comme texte d'abord
    };

    this.http.get(`${this.apiUrl}/${this.clientId}/services/virement/accounts`, xmlHttpOptions)
      .subscribe({
        next: (response: any) => {
          console.log('Réponse brute:', response);

          // Si c'est du XML, le parser
          if (typeof response === 'string' && response.includes('<PersistentBag>')) {
            this.clientAccounts = this.parseXmlAccounts(response);
          } else {
            // Si c'est déjà du JSON
            this.clientAccounts = response;
          }

          console.log('Comptes parsés:', this.clientAccounts);
          this.showMessage(`${this.clientAccounts.length} compte(s) chargé(s)`, 'success');
        },
        error: (error) => {
          console.error('Erreur lors du chargement des comptes:', error);
          this.showMessage('Erreur lors du chargement des comptes: ' + (error.error?.error || error.message), 'error');
        }
      });
  }

// Méthode pour parser le XML et extraire les comptes
  private parseXmlAccounts(xmlString: string): BankAccount[] {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, 'text/xml');
    const accounts: BankAccount[] = [];

    const items = xmlDoc.querySelectorAll('item');

    items.forEach(item => {
      const account: BankAccount = {
        id: this.getXmlElementText(item, 'id'),
        rib: this.getXmlElementText(item, 'rib'),
        balance: parseFloat(this.getXmlElementText(item, 'balance')) || 0,
        type: this.getXmlElementText(item, 'type')
      };
      accounts.push(account);
    });

    return accounts;
  }

  onBeneficiarySelect() {
    console.log('Bénéficiaire sélectionné:', this.selectedBeneficiaryId);

    if (this.selectedBeneficiaryId) {
      const selectedBeneficiary = this.beneficiaries.find(b => b.id.toString() === this.selectedBeneficiaryId);
      console.log('Bénéficiaire trouvé:', selectedBeneficiary);

      if (selectedBeneficiary) {
        this.transferRequest.toRib = selectedBeneficiary.rib;
        this.transferRequest.beneficiaryName = selectedBeneficiary.nom;
        this.ribValidation = null;
      }
    } else {
      this.transferRequest.toRib = '';
      this.transferRequest.beneficiaryName = '';
      this.ribValidation = null;
    }

    this.onFormChange();
  }
  onFormChange() {
    // Cette méthode peut être appelée sur les événements de changement pour forcer la réévaluation
    console.log('Form changed, current validity:', this.isTransferFormValid());
  }

  isTransferFormValid(): boolean {
    const hasAmount = this.transferRequest.amount > 0;
    const hasFromAccount = this.transferRequest.fromAccountId !== 0 && this.transferRequest.fromAccountId !== null;
    const hasBeneficiaryName = this.transferRequest.beneficiaryName.trim() !== '';

    if (this.destinationType === 'beneficiary') {
      const hasBeneficiarySelected = this.selectedBeneficiaryId !== null && this.selectedBeneficiaryId !== '';
      return hasAmount && hasFromAccount && hasBeneficiaryName && hasBeneficiarySelected;
    } else if (this.destinationType === 'manual') {
      const hasToRib = this.transferRequest.toRib.trim() !== '';
      return hasAmount && hasFromAccount && hasBeneficiaryName && hasToRib;
    }

    return false;
  }

// Méthode utilitaire pour extraire le texte d'un élément XML
  private getXmlElementText(parent: Element, tagName: string): any {
    const element = parent.querySelector(tagName);
    return element ? element.textContent : '';
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;

    switch(tab) {
      case 'virement':
        this.loadClientAccounts();
        this.loadBeneficiaries(); // Charger les bénéficiaires aussi
        break;
      case 'beneficiaires':
        this.loadBeneficiaries();
        this.loadRelationTypes(); // Charger les types de relation
        break;
      case 'historique':
        this.loadTransferHistory();
        break;
    }
  }

  validateRib() {
    if (!this.transferRequest.toRib || !this.clientId) return;

    this.http.post(`${this.apiUrl}/${this.clientId}/services/virement/validate-rib`,
      { rib: this.transferRequest.toRib }, this.httpOptions).subscribe({
      next: (data: any) => {
        this.ribValidation = data;
      },
      error: (error) => this.showMessage('Erreur lors de la validation du RIB', 'error')
    });
  }

  executeTransfer() {
    if (!this.clientId) return;

    this.http.post(`${this.apiUrl}/${this.clientId}/services/virement`,
      this.transferRequest, this.httpOptions).subscribe({
      next: (response: any) => {
        this.showMessage('Virement effectué avec succès!', 'success');
        this.resetTransferForm();
        this.loadClientAccounts();
      },
      error: (error) => this.showMessage('Erreur lors du virement: ' + error.error?.error, 'error')
    });
  }


  resetTransferForm() {
    this.transferRequest = {
      fromAccountId: 0,
      toRib: '',
      amount: 0,
      description: '',
      beneficiaryName: ''
    };
    this.ribValidation = null;
    this.destinationType = 'manual'; // Reset à manuel
    this.selectedBeneficiaryId = null;
  }
  debugLoadAccounts() {
    console.log('=== DEBUG LOAD ACCOUNTS ===');
    console.log('ClientId:', this.clientId);
    console.log('Token:', this.authService.getToken());
    console.log('Active tab:', this.activeTab);

    if (this.clientId) {
      this.loadClientAccounts();
    } else {
      console.error('Pas de clientId disponible');
    }
  }

  // Méthodes pour les bénéficiaires
  loadBeneficiaries() {
    if (!this.clientId) return;

    let url = `${this.apiUrl}/${this.clientId}/beneficiaries`;
    if (this.activeFilter !== '') {
      url += `?actif=${this.activeFilter}`;
    }

    this.http.get<Beneficiary[]>(url, this.httpOptions).subscribe({
      next: (data) => {
        this.beneficiaries = data;
        this.showMessage('Bénéficiaires chargés', 'success');
      },
      error: (error) => this.showMessage('Erreur lors du chargement des bénéficiaires', 'error')
    });
  }

  searchBeneficiaries() {
    if (!this.clientId) return;

    if (this.searchQuery.length < 2) {
      this.loadBeneficiaries();
      return;
    }

    this.http.get<Beneficiary[]>(`${this.apiUrl}/${this.clientId}/beneficiaries/search?query=${this.searchQuery}`,
      this.httpOptions).subscribe({
      next: (data) => {
        this.beneficiaries = data;
      },
      error: (error) => this.showMessage('Erreur lors de la recherche', 'error')
    });
  }

  loadRelationTypes() {
    if (!this.clientId) return;

    this.http.get(`${this.apiUrl}/${this.clientId}/beneficiaries/relation-types`,
      this.httpOptions).subscribe({
      next: (data) => {
        this.relationTypes = data;
        this.showMessage('Types de relation chargés', 'success');
      },
      error: (error) => this.showMessage('Erreur lors du chargement des types', 'error')
    });
  }

  loadBeneficiaryDetails(beneficiaryId: number) {
    if (!this.clientId) return;

    this.http.get(`${this.apiUrl}/${this.clientId}/beneficiaries/${beneficiaryId}`,
      this.httpOptions).subscribe({
      next: (data) => {
        console.log('Détails du bénéficiaire:', data);
        this.showMessage('Détails chargés (voir console)', 'success');
      },
      error: (error) => this.showMessage('Erreur lors du chargement des détails', 'error')
    });
  }

  editBeneficiary(beneficiary: Beneficiary) {
    this.editingBeneficiary = beneficiary;
    this.beneficiaryData = {
      nom: beneficiary.nom,
      relation: beneficiary.relation
    };
    this.showAddBeneficiary = true;
  }

  saveBeneficiary() {
    if (!this.clientId) return;

    if (this.editingBeneficiary) {
      // Modification
      this.http.put(`${this.apiUrl}/${this.clientId}/beneficiaries/${this.editingBeneficiary.id}`,
        this.beneficiaryData, this.httpOptions).subscribe({
        next: (response) => {
          this.showMessage('Bénéficiaire modifié avec succès', 'success');
          this.cancelBeneficiary();
          this.loadBeneficiaries();
        },
        error: (error) => this.showMessage('Erreur lors de la modification', 'error')
      });
    } else {
      // Ajout
      this.http.post(`${this.apiUrl}/${this.clientId}/beneficiaries`,
        this.beneficiaryData, this.httpOptions).subscribe({
        next: (response) => {
          this.showMessage('Bénéficiaire ajouté avec succès', 'success');
          this.cancelBeneficiary();
          this.loadBeneficiaries();
        },
        error: (error) => this.showMessage('Erreur lors de l\'ajout: ' + error.error?.error, 'error')
      });
    }
  }

  deleteBeneficiary(beneficiaryId: number) {
    if (!this.clientId) return;

    if (confirm('Êtes-vous sûr de vouloir supprimer ce bénéficiaire?')) {
      this.http.delete(`${this.apiUrl}/${this.clientId}/beneficiaries/${beneficiaryId}`,
        this.httpOptions).subscribe({
        next: (response) => {
          this.showMessage('Bénéficiaire supprimé avec succès', 'success');
          this.loadBeneficiaries();
        },
        error: (error) => this.showMessage('Erreur lors de la suppression', 'error')
      });
    }
  }

  cancelBeneficiary() {
    this.showAddBeneficiary = false;
    this.editingBeneficiary = null;
    this.beneficiaryData = { nom: '', rib: '', relation: '' };
  }

  // Méthodes pour l'historique
  loadTransferHistory() {
    if (!this.clientId) return;

    this.http.get<Transaction[]>(`${this.apiUrl}/${this.clientId}/services/virement/history?page=${this.historyPage}&size=${this.historySize}`,
      this.httpOptions).subscribe({
      next: (data) => {
        this.transferHistory = data;
        this.showMessage('Historique chargé', 'success');
      },
      error: (error) => this.showMessage('Erreur lors du chargement de l\'historique', 'error')
    });
  }

  // Utilitaires
  showMessage(msg: string, type: string) {
    this.message = msg;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }
}
