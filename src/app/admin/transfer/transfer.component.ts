import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { TransferService } from '../../services/transfer.service';
import { BankAccount } from '../models/bank-account.model';
import {NgClass} from '@angular/common';
import { CommonModule } from '@angular/common';

export interface TransferRequest {
  fromRib: string;
  toRib: string;
  amount: number;
}

export interface TransferResponse {
  success: boolean;
  message: string;
  fromAccount?: string;
  toAccount?: string;
  amount?: number;
  newBalance?: number;
}
export interface AccountInfo {
  id?: string;
  rib: string;
  accountNumber: string;
  balance: number;
  type: string;
  ownerName: string;
  createdAt?: string;
  updatedAt?: string;
  status?: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
  currency?: string;
}

export interface AccountResponse {
  found: boolean;
  message?: string;
  accountNumber?: string;
  balance?: number;
  type?: string;
  ownerName?: string;
}

@Component({
  selector: 'app-transfer',
  templateUrl: './transfer.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    NgClass
  ],
  styleUrls: ['./transfer.component.scss']
})
export class TransferComponent implements OnInit {
  transferForm: FormGroup;
  accounts: BankAccount[] = [];
  filteredFromAccounts: BankAccount[] = [];
  filteredToAccounts: BankAccount[] = [];

  fromAccountInfo: BankAccount | null = null;
  toAccountInfo: BankAccount | null = null;

  isLoading = false;
  showFromDropdown = false;
  showToDropdown = false;

  alertMessage = '';
  alertType: 'success' | 'error' = 'success';
  showAlert = false;

  constructor(private fb: FormBuilder, private transferService: TransferService) {
    this.transferForm = this.fb.group({
      fromRib: ['', [Validators.required, Validators.minLength(24)]],
      toRib:   ['', [Validators.required, Validators.minLength(24)]],
      amount:  ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.loadAccounts();
    this.setupFormListeners();
  }

  loadAccounts(): void {
    this.transferService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.filteredFromAccounts = [...accounts];
        this.filteredToAccounts   = [...accounts];
      },
      error: (err) => {
        console.error(err);
        this.showAlertMessage('Erreur lors du chargement des comptes', 'error');
      }
    });
  }

  setupFormListeners(): void {
    this.transferForm.get('fromRib')?.valueChanges.subscribe(val => {
      this.filteredFromAccounts = (val && val.length >= 3)
        ? this.accounts.filter(a =>
          a.rib.toLowerCase().includes(val.toLowerCase())
          || a.ownerName.toLowerCase().includes(val.toLowerCase())
        )
        : [...this.accounts];
      this.showFromDropdown = !!(val && this.filteredFromAccounts.length > 0);

      if (val?.length >= 24) this.getAccountInfo(val, 'from');
      else this.fromAccountInfo = null;
    });

    this.transferForm.get('toRib')?.valueChanges.subscribe(val => {
      this.filteredToAccounts = (val && val.length >= 3)
        ? this.accounts.filter(a =>
          a.rib.toLowerCase().includes(val.toLowerCase())
          || a.ownerName.toLowerCase().includes(val.toLowerCase())
        )
        : [...this.accounts];
      this.showToDropdown = !!(val && this.filteredToAccounts.length > 0);

      if (val?.length >= 24) this.getAccountInfo(val, 'to');
      else this.toAccountInfo = null;
    });
  }

  selectAccount(account: BankAccount, type: 'from' | 'to'): void {
    if (type === 'from') {
      this.transferForm.patchValue({ fromRib: account.rib });
      this.fromAccountInfo = account;
      this.showFromDropdown = false;
    } else {
      this.transferForm.patchValue({ toRib: account.rib });
      this.toAccountInfo = account;
      this.showToDropdown = false;
    }
  }

  getAccountInfo(rib: string, type: 'from' | 'to'): void {
    this.transferService.getAccountByRib(rib).subscribe({
      next: (resp) => {
        if (resp.found) {
          const acc: BankAccount = {
            rib: rib,
            accountNumber: resp.accountNumber!,
            balance: resp.balance!,
            type: resp.type!,
            ownerName: resp.ownerName!
          };
          type === 'from' ? this.fromAccountInfo = acc : this.toAccountInfo = acc;
        } else {
          if (type === 'from') this.fromAccountInfo = null;
          else this.toAccountInfo = null;
        }
      },
      error: (err) => console.error('getAccountInfo error', err)
    });
  }

  onSubmit(): void {
    if (this.transferForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading = true;
    const payload: TransferRequest = this.transferForm.value;

    this.transferService.transferFunds(payload).subscribe({
      next: (res: TransferResponse) => {
        this.isLoading = false;
        this.showAlertMessage(res.message, res.success ? 'success' : 'error');
        if (res.success) {
          this.transferForm.reset();
          this.fromAccountInfo = null;
          this.toAccountInfo = null;
          this.loadAccounts();
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Transfer error', err);
        this.showAlertMessage('Erreur lors du virement', 'error');
      }
    });
  }

  markFormGroupTouched(): void {
    Object.values(this.transferForm.controls).forEach(control => control.markAsTouched());
  }

  showAlertMessage(msg: string, type: 'success' | 'error'): void {
    this.alertMessage = msg;
    this.alertType = type;
    this.showAlert = true;
    setTimeout(() => this.showAlert = false, 5000);
  }

  closeAlert(): void {
    this.showAlert = false;
  }

  isFieldInvalid(field: string): boolean {
    const ctrl = this.transferForm.get(field);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }
  hideDropdownWithDelay(dropdownType: 'from' | 'to'): void {
    setTimeout(() => {
      if (dropdownType === 'from') {
        this.showFromDropdown = false;
      } else {
        this.showToDropdown = false;
      }
    }, 200);
  }

  getFieldError(field: string): string {
    const ctrl = this.transferForm.get(field);
    if (!ctrl?.errors) return '';
    if (ctrl.errors['required']) return 'Champ requis.';
    if (ctrl.errors['minlength']) return `Longueur min: ${ctrl.errors['minlength'].requiredLength}`;
    if (ctrl.errors['min']) return 'Le montant doit être ≥ 0.01';
    return 'Champ invalide.';
  }

  protected readonly setTimeout = setTimeout;
}