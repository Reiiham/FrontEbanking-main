import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-transaction-selector',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './transaction-selector.component.html',
  styleUrls: ['./transaction-selector.component.css']
})
export class TransactionSelectorComponent {}
