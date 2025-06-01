export interface AppSetting {
  id?: number;
  key: string;
  value: string | Date | boolean | number; // Union type pour gérer différents types de valeurs
  category?: string;
  label?: string;
  description?: string;
}
