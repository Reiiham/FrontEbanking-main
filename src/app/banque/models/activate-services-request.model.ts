export interface ActivateServicesRequest {
  clientId: string; // MUST be string to match backend
  services: string[];
}
