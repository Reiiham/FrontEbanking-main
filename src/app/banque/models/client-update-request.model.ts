export interface ClientUpdateRequest {
  clientId: number;
  newFirstName: string;
  newLastName: string;
  newEmail: string;
  newTel: string;
  supervisorPassword: string;
}
