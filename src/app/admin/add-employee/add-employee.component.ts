import { Component } from '@angular/core';
import { EmployeeService, EmployeeDTO } from '../../services/employee.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-add-employee',
  templateUrl: './add-employee.component.html',
  styleUrls: ['./add-employee.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ]
})
export class AddEmployeeComponent {
  employee: EmployeeDTO = {
    firstName: '',
    lastName: '',
    phoneNumber: '',
    cin: '',
    emailPersonnel: '',
  };
  successMessage: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private employeeService: EmployeeService, private router: Router) {}

  onSubmit() {
    if (!this.employee.firstName || !this.employee.lastName || !this.employee.phoneNumber || !this.employee.cin || !this.employee.emailPersonnel) {
      this.errorMessage = 'All fields are required.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.createEmployee(this.employee).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = `Employee created successfully! Identifiant: ${response.username}. `;
        this.employee = { firstName: '', lastName: '', phoneNumber: '' , cin: '', emailPersonnel: '' }; // Reset form
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Failed to create employee.';
        console.error('Error creating employee:', error);
      }
    });
  }

  cancel() {
    this.router.navigate(['/admin/dashboard']);
  }
}
