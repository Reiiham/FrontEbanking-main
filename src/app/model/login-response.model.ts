export interface LoginResponse {
  token: string;
  role: string;
  clientId: string;
  message?: string;
  requiresOtp?: boolean;
}
