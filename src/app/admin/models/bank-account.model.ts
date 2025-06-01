export interface BankAccount {
  id: string;
  type: string;
  status: string;
  balance: number;
  dateCreatedAt: string;
  overDraft?: number;
}
