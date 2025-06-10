import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientService } from '../../../services/banque.service';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { FormModule } from '@coreui/angular';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-client-delete',
  standalone: true,
  imports: [CommonModule,ToastrModule,FormsModule],
  templateUrl: './client-delete.component.html',
  styleUrls:['./client-delete.component.css']
})
export class ClientDeleteComponent implements OnInit {
  clientId = '';
  message = '';
  error = '';
  clientName = '';
  supervisorCode = '';


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clientService: ClientService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
  const id = this.route.snapshot.paramMap.get('id');
  if (id) {
    this.clientId = id;

    // Appel pour récupérer le nom du client
    this.clientService.getClientById(id).subscribe({
      next: (client) => {
        this.clientName = client.fullName;
      },
      error: () => {
        this.error = 'Erreur lors du chargement du client.';
      }
    });
  }
}


 confirmDelete(): void {
  const payload = {
    clientId: this.clientId,
    supervisorCode: this.supervisorCode
  };

  this.clientService.deleteClient(payload).subscribe({
    next: () => {
      this.toastr.success('Client supprimé avec succès.', 'Action auditée');
      this.router.navigate(['/clients']);
    },
    error: (err) => {
      if (err.status === 401) {
        this.toastr.error('Code superviseur incorrect.', 'Échec audit');
      } else if (err.status === 400) {
        this.toastr.warning(err.error, 'Suppression refusée');
      } else {
        this.toastr.error('Erreur inattendue.', 'Audit échoué');
      }
    }
  });
}




 cancel(): void {
  this.router.navigate(['/clients', this.clientId]);
}

}