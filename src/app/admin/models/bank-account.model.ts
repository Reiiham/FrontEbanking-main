/*
export interface BankAccount {
  id: string;
  type: string;
  status: string;
  balance: number;
  dateCreatedAt: string;
  overDraft?: number;
}
*/

export interface BankAccount {
  rib: string;
  accountNumber: string;
  balance: number;
  type: string;
  ownerName: string;
}
