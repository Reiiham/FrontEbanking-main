// src/app/components/employee-dashboard/employee-dashboard.component.ts
import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-employee-dashboard',
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css'],
  imports: [CommonModule, MatCardModule],
  standalone: true
})
export class EmployeeDashboardComponent {}
