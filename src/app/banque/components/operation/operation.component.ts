import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { OperationService } from '../../../services/operation.service';
import { ToastrService } from 'ngx-toastr';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-operation',
  imports: [FormsModule,ReactiveFormsModule],
  templateUrl: './operation.component.html',
  styleUrl: './operation.component.css'
})
export class OperationComponent {
  amount: number = 0;
  accountId: string = '';
  message: string = '';
  error: string = '';

  constructor(
    private opService: OperationService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  deposit() {
    this.opService.deposit(this.accountId, this.amount).subscribe({
      next: () => {
        this.message = '💰 Dépôt effectué avec succès';
        this.error = '';
        this.router.navigate(['/employee/clients']);
      },
      error: err => {
        this.error = err.error || 'Erreur lors du dépôt';
        this.message = '';
      }
    });
  }

  withdraw() {
    this.opService.withdraw(this.accountId, this.amount).subscribe({
      next: () => {
        this.message = '🏧 Retrait effectué avec succès';
        this.error = '';
        this.router.navigate(['/employee/clients']);
      },
      error: err => {
        this.error = err.error || 'Erreur lors du retrait';
        this.message = '';
      }
    });
  }

  onSubmit(): void {
    if (this.amount > 0) {
      this.deposit();
    } else if (this.amount < 0) {
      this.withdraw();
    } else {
      this.error = 'Veuillez entrer un montant différent de zéro.';
      this.message = '';
    }
  }
}

