import api from './api';

// Interfaces para tipagem
export interface TransactionDTO {
  id?: string;
  clientId: string;
  clientName?: string;
  appointmentId?: string;
  type: 'PAYMENT' | 'REFUND';
  amount: number;
  status: 'PENDING' | 'PAID' | 'REFUNDED' | 'CANCELLED';
  paymentMethod: string;
  paymentGatewayId?: string;
  invoiceUrl?: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RevenueSummaryDTO {
  totalRevenue: number;
  countPaid: number;
  countPending: number;
  averageTicket: number;
}

export interface RevenueByPeriodDTO {
  period: string;
  revenue: number;
}

export interface RevenueByPaymentMethodDTO {
  method: string;
  revenue: number;
  percentage: number;
}

export interface PaymentLinkDTO {
  transactionId: string;
  paymentUrl: string;
  expirationDate: string;
}

// URLs base para requisições
const BASE_URL = '/finance';
const TRANSACTIONS_URL = `${BASE_URL}/transactions`;
const REVENUE_URL = `${BASE_URL}/revenue`;

/**
 * Busca todas as transações
 */
export const getAllTransactions = async (): Promise<TransactionDTO[]> => {
  const response = await api.get(TRANSACTIONS_URL);
  return response.data;
};

/**
 * Busca uma transação por ID
 */
export const getTransactionById = async (id: string): Promise<TransactionDTO> => {
  const response = await api.get(`${TRANSACTIONS_URL}/${id}`);
  return response.data;
};

/**
 * Cria uma nova transação
 */
export const createTransaction = async (transaction: TransactionDTO): Promise<TransactionDTO> => {
  const response = await api.post(TRANSACTIONS_URL, transaction);
  return response.data;
};

/**
 * Atualiza uma transação existente
 */
export const updateTransaction = async (id: string, transaction: TransactionDTO): Promise<TransactionDTO> => {
  const response = await api.put(`${TRANSACTIONS_URL}/${id}`, transaction);
  return response.data;
};

/**
 * Remove uma transação
 */
export const deleteTransaction = async (id: string): Promise<void> => {
  await api.delete(`${TRANSACTIONS_URL}/${id}`);
};

/**
 * Processa um pagamento (muda status para PAID)
 */
export const processPayment = async (id: string): Promise<TransactionDTO> => {
  const response = await api.post(`${TRANSACTIONS_URL}/${id}/process`);
  return response.data;
};

/**
 * Processa um reembolso (para transações já pagas)
 */
export const processRefund = async (id: string): Promise<TransactionDTO> => {
  const response = await api.post(`${TRANSACTIONS_URL}/${id}/refund`);
  return response.data;
};

/**
 * Gera um link de pagamento para uma transação
 */
export const generatePaymentLink = async (id: string): Promise<PaymentLinkDTO> => {
  const response = await api.post(`${TRANSACTIONS_URL}/${id}/payment-link`);
  return response.data;
};

/**
 * Obtém um resumo geral da receita
 */
export const getRevenueSummary = async (): Promise<RevenueSummaryDTO> => {
  const response = await api.get(`${REVENUE_URL}/summary`);
  return response.data;
};

/**
 * Obtém receita por período
 * @param timeRange - Período de tempo (day, week, month, year)
 */
export const getRevenueByPeriod = async (timeRange: string): Promise<RevenueByPeriodDTO[]> => {
  const response = await api.get(`${REVENUE_URL}/by-period?timeRange=${timeRange}`);
  return response.data;
};

/**
 * Obtém receita por método de pagamento
 */
export const getRevenueByPaymentMethod = async (): Promise<RevenueByPaymentMethodDTO[]> => {
  const response = await api.get(`${REVENUE_URL}/by-payment-method`);
  return response.data;
};
