import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClientService } from '../../../services/banque.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-add-account',
  imports:[ReactiveFormsModule],
  templateUrl: './add-account.component.html',
  styleUrls: ['./add-account.component.css']
})
export class AddAccountComponent implements OnInit {
  accountForm!: FormGroup;
  clientId!: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private clientService: ClientService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.clientId = Number(this.route.snapshot.paramMap.get('id'));
    this.accountForm = this.fb.group({
      type: ['courant', Validators.required],
      balance: [0, [Validators.required, Validators.min(0.01)]]
    });
  }

  // submit(): void {
  //   if (this.accountForm.invalid) return;
  //
  //   const payload = {
  //     clientId: this.clientId,
  //     ...this.accountForm.value
  //   };

  submit(event?: Event): void {
    if (event) {
      event.preventDefault(); // Empêche la soumission native du formulaire
    }

    if (this.accountForm.invalid) return;

    const payload = {
      clientId: this.clientId,
      ...this.accountForm.value
    };

    this.clientService.addAccount(payload).subscribe({
      next: (res: any) => {
        this.toastr.success('✅ Compte ajouté avec succès');
        this.router.navigate(['/employee/clients', this.clientId]);
      },
      error: (err) => {
        console.error(err);
        this.toastr.error(err.error?.message || "Erreur lors de la création du compte");
      }
    });
  }
}
