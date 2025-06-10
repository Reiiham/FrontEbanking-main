import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsService } from '../../../services/settings.service';
import { AppSetting } from '../../models/app-setting.model';
import { SettingsEditFormComponent } from '../settings-edit-form/settings-edit-form.component';

@Component({
  selector: 'app-settings-list',
  standalone: true,
  imports: [CommonModule, SettingsEditFormComponent],
  templateUrl: './settings-list.component.html',
  styleUrls: ['./settings-list.component.css']
})
export class SettingsListComponent implements OnInit {
  settings: AppSetting[] = [];
  financeSettings: AppSetting[] = [];
  securitySettings: AppSetting[] = [];
  otherSettings: AppSetting[] = [];
  selectedSetting: AppSetting | null = null;
  isEditMode = false;
  loading: boolean = true;
  error: string | null = null;

  sortState: { [key: string]: { field: string; direction: 'asc' | 'desc' | null } } = {
    finance: { field: '', direction: null },
    security: { field: '', direction: null },
    other: { field: '', direction: null }
  };

  constructor(private settingsService: SettingsService) {}

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings() {
    this.loading = true;
    this.error = null;
    this.settingsService.getSettings().subscribe({
      next: (data: AppSetting[]) => {
        console.log('Settings loaded:', data);
        this.settings = data || [];
        this.classifySettings();
        this.loading = false;
      },
      error: (error: any) => {
        this.error = error.message || 'Erreur lors du chargement des paramètres. Vérifiez votre connexion ou contactez l\'administrateur.';
        this.loading = false;
        console.error('Error loading settings:', error);
      }
    });
  }

  classifySettings() {
    this.financeSettings = this.settings.filter(s => s.category?.toLowerCase() === 'finance');
    this.securitySettings = this.settings.filter(s => s.category?.toLowerCase() === 'security');
    this.otherSettings = this.settings.filter(s =>
      !s.category || (s.category.toLowerCase() !== 'finance' && s.category.toLowerCase() !== 'security')
    );
    this.sortCategory('finance');
    this.sortCategory('security');
    this.sortCategory('other');
  }

  sortSettings(category: string, field: string) {
    const currentSort = this.sortState[category];
    if (currentSort.field === field && currentSort.direction === 'asc') {
      currentSort.direction = 'desc';
    } else if (currentSort.field === field && currentSort.direction === 'desc') {
      currentSort.direction = null;
    } else {
      currentSort.field = field;
      currentSort.direction = 'asc';
    }
    this.sortCategory(category);
  }

  sortCategory(category: string) {
    const sortState = this.sortState[category];
    let settingsArray: AppSetting[];

    if (category === 'finance') settingsArray = this.financeSettings;
    else if (category === 'security') settingsArray = this.securitySettings;
    else settingsArray = this.otherSettings;

    if (!sortState.direction) return;

    settingsArray.sort((a, b) => {
      let valueA = a[sortState.field as keyof AppSetting];
      let valueB = b[sortState.field as keyof AppSetting];

      if (sortState.field === 'value') {
        valueA = this.formatValue(valueA);
        valueB = this.formatValue(valueB);
      }

      if (valueA === null || valueA === undefined) valueA = '';
      if (valueB === null || valueB === undefined) valueB = '';

      if (sortState.direction === 'asc') {
        return String(valueA).localeCompare(String(valueB));
      } else {
        return String(valueB).localeCompare(String(valueA));
      }
    });
  }

  getSortIcon(category: string, field: string): string {
    const sortState = this.sortState[category];
    if (sortState.field !== field || !sortState.direction) return '↕️';
    return sortState.direction === 'asc' ? '↑' : '↓';
  }

  addNewSetting() {
    this.selectedSetting = {
      key: '',
      value: '',
      category: 'other',
      label: '',
      description: ''
    };
    this.isEditMode = true;
  }

  editSetting(setting: AppSetting) {
    this.selectedSetting = { ...setting };
    this.isEditMode = true;
  }

  onSaveSetting(updatedAppSetting: AppSetting) {
    if (updatedAppSetting.id !== undefined && updatedAppSetting.id !== null) {
      this.settingsService.updateSetting(updatedAppSetting.id, updatedAppSetting).subscribe({
        next: (updatedSetting: AppSetting) => {
          const index = this.settings.findIndex(s => s.id === updatedSetting.id);
          if (index !== -1) {
            this.settings[index] = updatedSetting;
            this.classifySettings();
          }
          this.cancelEdit();
        },
        error: (error: any) => {
          let errorMessage = 'Erreur lors de la mise à jour du paramètre.';
          if (error.status === 0) {
            errorMessage += ' Problème de connexion au serveur. Vérifiez que le serveur est en cours d\'exécution.';
          } else if (error.status) {
            errorMessage += ` Code: ${error.status}. ${error.message || 'Erreur inconnue.'}`;
          }
          this.error = errorMessage;
          console.error('Error updating setting:', error);
        }
      });
    } else {
      this.settingsService.addSetting(updatedAppSetting).subscribe({
        next: (newSetting: AppSetting) => {
          this.settings = [...this.settings, newSetting];
          this.classifySettings();
          this.cancelEdit();
        },
        error: (error: any) => {
          let errorMessage = 'Erreur lors de l\'ajout du paramètre.';
          if (error.status === 0) {
            errorMessage += ' Problème de connexion au serveur. Vérifiez que le serveur est en cours d\'exécution.';
          } else if (error.status) {
            errorMessage += ` Code: ${error.status}. ${error.message || 'Erreur inconnue.'}`;
          }
          this.error = errorMessage;
          console.error('Error adding setting:', error);
        }
      });
    }
  }

  deleteSetting(id: number) {
    if (id === undefined || id === null) {
      console.warn('Cannot delete setting: ID is undefined or null');
      return;
    }
    if (confirm('Êtes-vous sûr de vouloir supprimer ce paramètre ?')) {
      this.settingsService.deleteSetting(id).subscribe({
        next: () => {
          this.settings = this.settings.filter(s => s.id !== id);
          this.classifySettings();
          if (this.selectedSetting?.id === id) {
            this.cancelEdit();
          }
        },
        error: (error: any) => {
          let errorMessage = 'Erreur lors de la suppression du paramètre.';
          if (error.status === 0) {
            errorMessage += ' Problème de connexion au serveur. Vérifiez que le serveur est en cours d\'exécution.';
          } else if (error.status) {
            errorMessage += ` Code: ${error.status}. ${error.message || 'Erreur inconnue.'}`;
          }
          this.error = errorMessage;
          console.error('Error deleting setting:', error);
        }
      });
    }
  }

  cancelEdit() {
    this.selectedSetting = null;
    this.isEditMode = false;
  }

  formatValue(value: any): string {
    if (value === null || value === undefined) {
      return '-';
    }
    if (typeof value === 'boolean') {
      return value ? 'Oui' : 'Non';
    }
    if (value instanceof Date && !isNaN(value.getTime())) {
      return value.toLocaleDateString('fr-FR');
    }
    if (typeof value === 'number' || (typeof value === 'string' && !isNaN(Number(value)) && value.trim() !== '')) {
      return String(value);
    }
    if (typeof value === 'object') {
      try {
        return JSON.stringify(value);
      } catch (e) {
        return '[Objet complexe]';
      }
    }
    return String(value);
  }
}
