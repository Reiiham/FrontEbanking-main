import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Currency } from '../../models/currency.model';
import { CurrencyService } from '../../../services/currency.service';
import { CurrencyFormComponent } from '../currency-form/currency-form.component';

@Component({
  selector: 'app-currency-list',
  standalone: true,
  imports: [CommonModule, CurrencyFormComponent],
  templateUrl: './currency-list.component.html',
  styleUrls: ['./currency-list.component.css']
})
export class CurrencyListComponent implements OnInit {
  currencies: Currency[] = [];
  selectedCurrency: Currency | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private currencyService: CurrencyService) {}

  ngOnInit(): void {
    this.loadCurrencies();
  }

  loadCurrencies(): void {
    this.loading = true;
    this.error = null;
    this.currencyService.getAllCurrencies().subscribe({
      next: (data: Currency[]) => {
        this.currencies = data;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = `Erreur lors du chargement des devises: ${err.message || 'Erreur inconnue'}`;
        this.loading = false;
        console.error('Error loading currencies:', err);
      }
    });
  }

  addNewCurrency(): void {
    this.selectedCurrency = {
      name: '',
      codeISO: '',
      exchangeRate: 1.0
    };
  }

  edit(currency: Currency): void {
    this.selectedCurrency = { ...currency };
  }

  saveEdited(currency: Currency): void {
    this.error = null;

    if (currency.id) {
      // Update existing currency
      this.currencyService.updateCurrency(currency.id, currency).subscribe({
        next: (updated: Currency) => {
          const index = this.currencies.findIndex(c => c.id === updated.id);
          if (index !== -1) {
            this.currencies[index] = updated;
          }
          this.selectedCurrency = null;
        },
        error: (err: any) => {
          this.error = `Erreur lors de la mise à jour de la devise: ${err.message || 'Erreur inconnue'}`;
          console.error('Error updating currency:', err);
        }
      });
    } else {
      // Add new currency
      this.currencyService.addCurrency(currency).subscribe({
        next: (added: Currency) => {
          this.currencies.push(added);
          this.selectedCurrency = null;
        },
        error: (err: any) => {
          this.error = `Erreur lors de l'ajout de la devise: ${err.message || 'Erreur inconnue'}`;
          console.error('Error adding currency:', err);
        }
      });
    }
  }

  cancelEdit(): void {
    this.selectedCurrency = null;
  }

  deleteCurrency(id: number): void {
    if (id === undefined || id === null) {
      console.warn('Cannot delete currency: ID is undefined or null');
      return;
    }
    if (confirm('Êtes-vous sûr de vouloir supprimer cette devise ?')) {
      this.currencyService.deleteCurrency(id).subscribe({
        next: () => {
          this.currencies = this.currencies.filter(c => c.id !== id);
          if (this.selectedCurrency?.id === id) {
            this.selectedCurrency = null;
          }
        },
        error: (err: any) => {
          this.error = `Erreur lors de la suppression de la devise: ${err.message || 'Erreur inconnue'}`;
          console.error('Error deleting currency:', err);
        }
      });
    }
  }
}
