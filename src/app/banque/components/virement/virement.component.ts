import { Component } from '@angular/core';
import {ReactiveFormsModule, FormGroup, Validators, FormBuilder} from '@angular/forms';
import { VirementService } from '../../../services/virement.service';

@Component({
  selector: 'app-virement',
  templateUrl: './virement.component.html',
  imports: [
    ReactiveFormsModule
  ],
  styleUrls: ['./virement.component.css']
})
export class VirementComponent {
  virementForm: FormGroup;
  successMessage = '';
  errorMessage = '';

  constructor(private fb: FormBuilder, private virementService: VirementService) {
    this.virementForm = this.fb.group({
      fromAccountId: ['', Validators.required],
      toRib: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(1)]]
    });
  }

  effectuerVirement(): void {
    if (this.virementForm.invalid) return;

    const data = this.virementForm.value;

    this.virementService.effectuerVirement(data).subscribe({
      next: res => {
        this.successMessage = res;
        this.errorMessage = '';
        this.virementForm.reset();
      },
      error: err => {
        this.errorMessage = err.error || 'Erreur inconnue';
        this.successMessage = '';
      }
    });
  }
}
