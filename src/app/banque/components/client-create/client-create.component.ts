import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ClientService } from '../../../services/banque.service';
import { ToastrService } from 'ngx-toastr';


@Component({
  selector: 'app-client-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './client-create.component.html',
  styleUrls:['./client-create.component.css']
})
export class ClientCreateComponent {
  firstName: string = '';
  lastName: string = '';
  cin: string = '';
  email: string = '';
  tel: string = '';
  birthDate: string = '';
  accountType: string = 'courant';
  balance: number = 0;
  documentsComplets: boolean = false;
  compteBloque: boolean = false;


  message: string = '';
  error: string = '';

  constructor(private clientService: ClientService,
     private router: Router,
    private toastr: ToastrService
  ) {}

formatDate(dateString: string): string {
  if (!dateString) return '';
  const date = new Date(dateString);
  return isNaN(date.getTime()) ? '' : date.toISOString().split('T')[0];
}




  create(): void {
    this.error = '';
    const payload = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      tel: this.tel,
      birthDate: this.formatDate(this.birthDate), // ✅ formaté
      accountType: this.accountType,
      balance: this.balance,
      cin: this.cin
    };


    this.clientService.createClient(payload).subscribe({
      next: () => {
        this.message = 'Client créé avec succès.';
        this.toastr.success('Client créé avec succès.', 'Audit log');
        this.router.navigate(['/employee/clients']);
      },
      error: () => {
        this.error = 'Erreur lors de la création du client.';
        this.toastr.error('Erreur lors de la création.', 'Audit échoué');
      }
    });
  }
  showRecap = false;

toggleRecap(): void {
  this.showRecap = !this.showRecap;
}

}
