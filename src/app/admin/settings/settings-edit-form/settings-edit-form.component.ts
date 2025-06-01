import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppSetting } from '../../models/app-setting.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-settings-edit-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings-edit-form.component.html',
  styleUrls: ['./settings-edit-form.component.css']
})
export class SettingsEditFormComponent {
  @Input() setting!: AppSetting;
  @Output() save = new EventEmitter<AppSetting>();
  @Output() cancel = new EventEmitter<void>();
  @Output() delete = new EventEmitter<number>();

  get formTitle(): string {
    return this.setting.id ? 'Modifier le paramètre' : 'Ajouter un paramètre';
  }

  onSubmit(): void {
    this.save.emit({ ...this.setting });
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onDelete(): void {
    if (this.setting.id) {
      this.delete.emit(this.setting.id);
    }
  }

  isValueDate(): boolean {
    // Assume value is a date if it matches a date-like string (e.g., YYYY-MM-DD)
    return typeof this.setting.value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(this.setting.value);
  }
}
