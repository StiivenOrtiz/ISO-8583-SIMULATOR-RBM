export type TransactionProtocol = 'ISO8583' | 'HTTP';

export interface Transaction {
  id: string;
  uuid: string;
  txTimestamp: string;
  terminal: string;
  amount: number;
  franchise: string;
  franchiseLogo: string;
  transactionType: string;
  mti: string;
  protocol?: TransactionProtocol;
  status: "success" | "failed" | "pending";
  responseCode: string;
  authCode?: string; // P38
  rrn?: string; // P37
  bitmapPrimary: string;
  bitmapSecondary?: string;
  requestDataElements?: Record<string, string>;
  responseDataElements?: Record<string, string>;
  hexRequest?: string;
  hexResponse?: string;
}

export interface TransactionFilter {
  terminal?: string;
  franchise?: string;
  transactionType?: string;
  mti?: string;
  status?: string;
  responseCode?: string;
  authCode?: string;
  rrn?: string;
  authCodeEmpty?: boolean;
  rrnEmpty?: boolean;
  responseCodeEmpty?: boolean;
  dateFrom?: string;
  dateTo?: string;
  amountFrom?: number;
  amountTo?: number;
  searchText?: string;
}


export interface TransactionFilterMetadata {
  terminals: string[];
  franchises: string[];
  transactionTypes: string[];
  mtis: string[];
  statusValues: string[];
}
