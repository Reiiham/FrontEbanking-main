import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { BankAccount } from '../../admin/models/bank-account.model';

@Component({
  selector: 'app-account-details',
  templateUrl: './account-details.component.html',
  styleUrls: ['./account-details.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatButtonModule]
})
export class AccountDetailsComponent implements OnInit {
  clientId: string | null = null;
  account: BankAccount | null = null;
  errorMessage: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('clientId');
    console.log('AccountDetails clientId:', this.clientId);
    this.route.data.subscribe({
      next: ({ account }) => {
        this.account = account;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to load account details';
      }
    });
  }
}
