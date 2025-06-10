import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { RechargeService, PhoneRechargeRequest, PhoneRechargeResponse, Operator, PhoneValidationResponse, AccountInfo, DailyLimits, RechargeHistory } from '../services/recharge.service';
import { CryptoService, CryptoTransaction, CryptoRate, CryptoBalance, CryptoStats, PortfolioValue, DepositAddress } from '../services/crypto.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ChatbotComponent } from '../components/chatbot/chatbot.component';

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
  templateUrl: 'client-dashboard.component.html',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ChatbotComponent,
  ],
  styleUrl: 'client-dashboard.component.scss',
})
export class ClientDashboardComponent implements OnInit, OnDestroy {
  clientId: string | null = null;
  private _activeTab: string = 'virement';
  private subscriptions: Subscription[] = [];

  // Données pour les virements
  clientAccounts: BankAccount[] = [];
  destinationType: string = 'manual';
  selectedBeneficiaryId: string | null = null;
  transferRequest: TransferRequest = {
    fromAccountId: 0,
    toRib: '',
    amount: 0,
    description: '',
    beneficiaryName: ''
  };
  ribValidation: any = null;

  // Données pour les bénéficiaires
  beneficiaries: Beneficiary[] = [];
  selectedBeneficiary: Beneficiary | null = null; // New property to store selected beneficiary details
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

  // Données pour les recharges
  operators: Operator[] = [];
  rechargeRequest: PhoneRechargeRequest = {
    phoneNumber: '',
    operatorCode: '',
    amount: 0,
    transactionPin: ''
  };
  phoneValidation: PhoneValidationResponse | null = null;
  accountInfo: AccountInfo | null = null;
  dailyLimits: DailyLimits | null = null;
  rechargeHistory: RechargeHistory | null = null;

  // Données pour les cryptos
  cryptoBalance: CryptoBalance = {};
  cryptoRates: { [key: string]: number } = {};
  cryptoStats: CryptoStats[] = [];
  portfolioValue: number = 0;
  cryptoTransaction: { symbol: string, usdAmount: number, quantity: number, side: string } = { symbol: '', usdAmount: 0, quantity: 0, side: 'BUY' };
  cryptoHistory: CryptoTransaction[] = [];
  supportedSymbols: string[] = [];

  // Messages
  message: string = '';
  messageType: string = '';

  private apiUrl = 'http://localhost:8090/eBankingVer1_war_exploded/api/clients';
  private httpOptions: { headers: HttpHeaders } = { headers: new HttpHeaders() };

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private rechargeService: RechargeService,
    private cryptoService: CryptoService
  ) {}

  private onTabChange(tab: string) {
      switch (tab) {
        case 'virement':
          if (this.clientId) {
            this.loadClientAccounts();
            this.loadBeneficiaries();
          }
          break;
        case 'beneficiaires':
          if (this.clientId) {
            this.loadBeneficiaries();
            this.loadRelationTypes();
          }
          break;
        case 'historique':
          if (this.clientId) {
            this.loadTransferHistory();
          }
          break;
        case 'recharge':
          this.loadRechargeData();
          break;
        case 'crypto':
          this.loadCryptoData();
          break;
        case 'qr': // ✅ Ajouter ce bloc
          if (this.clientId) {
            this.loadClientAccounts();
          }
          break;
      }
    }



      qrData = {
      rib: '',
      amount: 0,
      description: ''
    };
    qrImage: string | null = null;
    qrError: string | null = null;

    generateQr() {

      
    console.log('Token:', this.authService.getToken());

  const headers = new HttpHeaders({
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Authorization': `Bearer ${this.authService.getToken()}`
  });
  const body = {
    rib: this.qrData.rib,
    amount: this.qrData.amount,
    description: this.qrData.description
  };
  console.log('Payload:', body);


  this.http.post<any>('http://localhost:8090/eBankingVer1_war_exploded/api/v1/qr-payments/generate', body, { headers })
    .subscribe({
      next: (res) => {
        if (res.success) {
          this.qrImage = res.data; // base64 image
          this.qrError = null;
        } else {
          this.qrError = res.error || 'Erreur inconnue';
          this.qrImage = null;
        }
      },
      error: (err) => {
        this.qrError = err.error?.error || 'Erreur réseau ou serveur';
        this.qrImage = null;
      }
    });
}



  qrPaymentData = {
  sourceAccountId: '',  // Utilisez l'ID du compte directement
  rib: '',
  amount: 0,
  description: ''
};

processQrPayment() {
  console.log('Selected account ID:', this.qrPaymentData.sourceAccountId);

  const headers = new HttpHeaders({
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Authorization': `Bearer ${this.authService.getToken()}`
  });

  const body = { ...this.qrPaymentData };

  this.http.post<any>(
    'http://localhost:8090/eBankingVer1_war_exploded/api/v1/qr-payments/process',
    body,
    { headers }
  ).subscribe({
    next: (res) => {
      if (res.success) {
        this.showMessage('Paiement QR effectué avec succès ✅', 'success');
        this.qrPaymentData = { sourceAccountId: '', rib: '', amount: 0, description: '' };
        this.loadClientAccounts(); // Refresh balance
      } else {
        this.showMessage(res.error || 'Erreur lors du traitement du paiement QR', 'error');
      }
    },
    error: (err) => {
      this.showMessage(err.error?.error || 'Erreur serveur', 'error');
    }
  });
}

useQrToPay() {
  this.qrPaymentData.rib = this.qrData.rib;
  this.qrPaymentData.amount = this.qrData.amount;
  this.qrPaymentData.description = this.qrData.description;
  this.activeTab = 'qr'; // reste sur le même onglet pour visibilité
  this.showMessage('Formulaire de paiement prérempli avec les données du QR.', 'success');
}

isQrPaymentValid(): boolean {
  return (
    this.qrPaymentData.sourceAccountId !== '' &&
    this.qrPaymentData.rib.trim() !== '' &&
    this.qrPaymentData.amount > 0
  );
}





  ngOnInit() {
    const clientIdSub = this.authService.clientId$.subscribe(clientId => {
      this.clientId = clientId;
      if (clientId) {
        this.updateHttpOptions();
        this.initializeData();
      }
    });
    this.subscriptions.push(clientIdSub);

    const currentClientId = this.authService.getClientId();
    if (currentClientId) {
      this.clientId = currentClientId;
      this.updateHttpOptions();
      this.initializeData();
    }
  }

  get activeTab(): string {
    return this._activeTab;
  }

  set activeTab(value: string) {
    this._activeTab = value;
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
    this.onTabChange(this.activeTab);
  }

  // Méthodes pour les virements
  loadClientAccounts() {
    console.log(this.clientAccounts)
    if (!this.clientId) {
      this.showMessage('ClientId non disponible', 'error');
      return;
    }
    const xmlHttpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/xml, application/json',
        'Authorization': this.authService.getToken() ? `Bearer ${this.authService.getToken()}` : ''
      }),
      responseType: 'text' as 'json'
    };
    this.http.get(`${this.apiUrl}/${this.clientId}/services/virement/accounts`, xmlHttpOptions)
      .subscribe({
        next: (response: any) => {
          if (typeof response === 'string' && response.includes('<PersistentBag>')) {
            this.clientAccounts = this.parseXmlAccounts(response);
          } else {
            this.clientAccounts = response;
          }
          this.showMessage(`${this.clientAccounts.length} compte(s) chargé(s)`, 'success');
        },
        error: (error) => this.showMessage('Erreur lors du chargement des comptes: ' + (error.error?.error || error.message), 'error')
      });
  }

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

  private getXmlElementText(parent: Element, tagName: string): any {
    const element = parent.querySelector(tagName);
    return element ? element.textContent : '';
  }

  onBeneficiarySelect() {
    if (this.selectedBeneficiaryId) {
      const selectedBeneficiary = this.beneficiaries.find(b => b.id.toString() === this.selectedBeneficiaryId);
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
    this.destinationType = 'manual';
    this.selectedBeneficiaryId = null;
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
    if (!this.clientId) {
      this.showMessage('Client ID non disponible', 'error');
      return;
    }
    this.http.get<Beneficiary>(`${this.apiUrl}/${this.clientId}/beneficiaries/${beneficiaryId}`, this.httpOptions).subscribe({
      next: (data: Beneficiary) => {
        this.selectedBeneficiary = data;
        this.showMessage(`Détails du bénéficiaire ${data.nom} chargés`, 'success');
      },
      error: (error) => this.showMessage('Erreur lors du chargement des détails: ' + (error.error?.error || error.message), 'error')
    });
  }

  clearBeneficiaryDetails() {
    this.selectedBeneficiary = null;
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

  // Méthodes pour les recharges
  loadRechargeData() {
    this.rechargeService.getSupportedOperators().subscribe({
      next: (operators) => this.operators = operators,
      error: (error) => this.showMessage('Erreur lors du chargement des opérateurs: ' + error.message, 'error')
    });
    this.rechargeService.getAccountInfo().subscribe({
      next: (info) => this.accountInfo = info,
      error: (error) => this.showMessage('Erreur lors du chargement des infos du compte: ' + error.message, 'error')
    });
    this.rechargeService.getDailyLimits().subscribe({
      next: (limits) => this.dailyLimits = limits,
      error: (error) => this.showMessage('Erreur lors du chargement des limites: ' + error.message, 'error')
    });
    this.rechargeService.getRechargeHistory().subscribe({
      next: (history) => this.rechargeHistory = history,
      error: (error) => this.showMessage('Erreur lors du chargement de l\'historique: ' + error.message, 'error')
    });
  }

  validatePhoneNumber() {
    if (!this.rechargeRequest.phoneNumber || !this.rechargeRequest.operatorCode) return;
    this.rechargeService.validatePhoneNumber({
      phoneNumber: this.rechargeRequest.phoneNumber,
      operatorCode: this.rechargeService.detectOperatorFromNumber(this.rechargeRequest.phoneNumber) || this.rechargeRequest.operatorCode
    }).subscribe({
      next: (response) => {
        this.phoneValidation = response;
        this.showMessage(response.valid ? 'Numéro valide' : 'Numéro invalide', response.valid ? 'success' : 'error');
      },
      error: (error) => this.showMessage('Erreur lors de la validation du numéro: ' + error.message, 'error')
    });
  }

  executeRecharge() {
    this.rechargeService.rechargePhone(this.rechargeRequest).subscribe({
      next: (response) => {
        this.showMessage('Recharge effectuée avec succès!', 'success');
        this.resetRechargeForm();
        this.loadRechargeData();
      },
      error: (error) => this.showMessage('Erreur lors de la recharge: ' + error.message, 'error')
    });
  }

  resetRechargeForm() {
    this.rechargeRequest = {
      phoneNumber: '',
      operatorCode: '',
      amount: 0,
      transactionPin: ''
    };
    this.phoneValidation = null;
  }

  // Méthodes pour les cryptos
  loadCryptoData() {
    this.cryptoService.getSupportedSymbols().forEach(symbol => this.supportedSymbols.push(symbol));
    this.cryptoService.getCryptoBalance().subscribe({
      next: (balance) => this.cryptoBalance = balance,
      error: (error) => this.showMessage('Erreur lors du chargement du solde crypto: ' + error.message, 'error')
    });
    this.cryptoService.getAllRates().subscribe({
      next: (rates) => this.cryptoRates = rates,
      error: (error) => this.showMessage('Erreur lors du chargement des taux: ' + error.message, 'error')
    });
    this.cryptoService.getCryptoStats().subscribe({
      next: (stats) => this.cryptoStats = stats,
      error: (error) => this.showMessage('Erreur lors du chargement des stats: ' + error.message, 'error')
    });
    this.cryptoService.getPortfolioValue().subscribe({
      next: (value) => this.portfolioValue = value.portfolioUsdValue,
      error: (error) => this.showMessage('Erreur lors du chargement de la valeur du portfolio: ' + error.message, 'error')
    });
    this.cryptoService.getTransactionHistory().subscribe({
      next: (history) => this.cryptoHistory = history,
      error: (error) => this.showMessage('Erreur lors du chargement de l\'historique crypto: ' + error.message, 'error')
    });
  }

  executeCryptoTrade() {
    const { symbol, usdAmount, quantity, side } = this.cryptoTransaction;
    if (side === 'BUY') {
      this.cryptoService.buyCrypto(symbol, usdAmount).subscribe({
        next: () => {
          this.showMessage('Achat effectué avec succès!', 'success');
          this.resetCryptoForm();
          this.loadCryptoData();
        },
        error: (error) => this.showMessage('Erreur lors de l\'achat: ' + error.message, 'error')
      });
    } else {
      this.cryptoService.sellCrypto(symbol, quantity).subscribe({
        next: () => {
          this.showMessage('Vente effectuée avec succès!', 'success');
          this.resetCryptoForm();
          this.loadCryptoData();
        },
        error: (error) => this.showMessage('Erreur lors de la vente: ' + error.message, 'error')
      });
    }
  }

  resetCryptoForm() {
    this.cryptoTransaction = { symbol: '', usdAmount: 0, quantity: 0, side: 'BUY' };
  }

  // Utilitaires
  showMessage(msg: string, type: string) {
    this.message = msg;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  parseCustomDate(dateValue: any): string {
    if (!dateValue) return '-';
    try {
      let parts: number[];
      if (Array.isArray(dateValue)) {
        parts = dateValue;
      } else if (typeof dateValue === 'string') {
        parts = dateValue.split(',').map(part => parseInt(part.trim()));
      } else if (dateValue instanceof Date) {
        return dateValue.toLocaleDateString('fr-FR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
      } else {
        const dateString = String(dateValue);
        if (dateString.includes(',')) {
          parts = dateString.split(',').map(part => parseInt(part.trim()));
        } else {
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
      if (parts.length < 6) {
        return String(dateValue);
      }
      const date = new Date(
        parts[0],
        parts[1] - 1,
        parts[2],
        parts[3],
        parts[4],
        parts[5],
        parts[6] ? Math.floor(parts[6] / 1000000) : 0
      );
      if (isNaN(date.getTime())) {
        return String(dateValue);
      }
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
}