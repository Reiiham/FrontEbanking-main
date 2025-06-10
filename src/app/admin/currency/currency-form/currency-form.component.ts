import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Currency } from '../../models/currency.model';
import { FormsModule } from '@angular/forms';
import {CURRENCY_LIST} from '../CURRENCY_LIST';



@Component({
  selector: 'app-currency-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './currency-form.component.html',
  styleUrls: ['./currency-form.component.css']
})
export class CurrencyFormComponent {
  currencyList = CURRENCY_LIST;
  @Input() formTitle: string = '';
  @Input() currency!: Currency;
  @Output() save = new EventEmitter<Currency>();
  @Output() cancel = new EventEmitter<void>();
  @Output() delete = new EventEmitter<number>();
  onSelectCurrency(event: Event): void {
    const selectedCode = (event.target as HTMLSelectElement).value;
    const selected = this.currencyList.find(c => c.code === selectedCode);
    if (selected) {
      this.currency.codeISO = selected.code;
      this.currency.name = selected.name;
    }
  }

  onSubmit(): void {
    this.save.emit({ ...this.currency });
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onDelete(): void {
    if (this.currency.id) {
      this.delete.emit(this.currency.id);
    }
  }
}
