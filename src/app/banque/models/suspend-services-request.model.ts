export interface SuspendServicesRequest {
  servicesToSuspend: string[];
  reason: string;
  notificationMessage: string;
}
