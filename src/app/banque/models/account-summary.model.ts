import { TransactionDTO } from './transaction.model';

export interface AccountSummaryDTO {
  accountNumber: string;
  type: string;
  balance: number;
  transactions: TransactionDTO[];
}
