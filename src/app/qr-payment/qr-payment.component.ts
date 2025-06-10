import { Component } from '@angular/core';
import { QrPaymentService } from '../services/qr-payment.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-qr-payment',
  imports: [ReactiveFormsModule],
  templateUrl: './qr-payment.component.html',
  styleUrl: './qr-payment.component.css'
})
export class QrPaymentComponent {
  qrForm: FormGroup;
  qrImageSrc: string | null = null;
  error: string | null = null;

  constructor(private qrPaymentService: QrPaymentService, private fb: FormBuilder) {
    this.qrForm = this.fb.group({
      rib: ['', [Validators.required, Validators.pattern(/^\d{21}$/)]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', Validators.maxLength(100)]
    });
  }

  generateQr() {
    if (this.qrForm.invalid) {
      this.error = 'Veuillez remplir correctement le formulaire';
      return;
    }

    const request = this.qrForm.value;
    this.qrPaymentService.generateQr(request).subscribe({
      next: (response) => {
        if (response.success) {
          this.qrImageSrc = response.data;
          this.error = null;
        } else {
          this.error = response.error || 'Erreur lors de la génération du QR';
          this.qrImageSrc = null;
        }
      },
      error: (err) => {
        this.error = 'Erreur serveur : ' + err.message;
        this.qrImageSrc = null;
      }
    });
  }
}
