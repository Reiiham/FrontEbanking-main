import {AccountOperation} from './account-operation.model';
export interface AccountHistory {
  accountOperationDTOS: AccountOperation[];
  accountId: string;
  balance: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  total: number;
}
