export interface AccountOperation {
  date: string;
  type: 'DEBIT' | 'CREDIT';
  amount: number;
  description: string;
}
