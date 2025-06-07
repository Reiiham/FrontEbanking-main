export interface TransactionDTO {
  transactionId: string;
  amount: number;
  date: string; // or `Date` if you're parsing it
}
