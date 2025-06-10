import { AccountSummaryDTO } from './account-summary.model';

export interface ClientSummaryDTO {
  clientId: number;
  fullName: string;
  accounts: AccountSummaryDTO[];

  compteBloque: boolean;
  documentsComplets: boolean;
}
