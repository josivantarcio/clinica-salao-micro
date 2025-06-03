import { api } from './api';

export interface ReportFilter {
  startDate?: string;
  endDate?: string;
  clientId?: number;
  professionalId?: number;
  serviceType?: string;
  status?: string;
}

export interface FinancialSummary {
  totalRevenue: number;
  totalExpenses: number;
  netRevenue: number;
  averageTicket: number;
  pendingPayments: number;
  refunds: number;
  period: {
    startDate: string;
    endDate: string;
  };
}

export interface RevenueTrend {
  date: string;
  amount: number;
}

export interface ServicePopularity {
  serviceName: string;
  count: number;
  revenue: number;
  percentage: number;
}

export interface ProfessionalPerformance {
  professionalId: number;
  professionalName: string;
  totalAppointments: number;
  completedAppointments: number;
  canceledAppointments: number;
  totalRevenue: number;
  clientSatisfaction: number;
}

export interface ClientMetrics {
  totalClients: number;
  newClients: number;
  returningClients: number;
  averageVisitsPerClient: number;
  topClients: {
    clientId: number;
    clientName: string;
    totalAppointments: number;
    totalSpent: number;
  }[];
}

export interface ClientRetention {
  month: string;
  retentionRate: number;
  newClients: number;
  lostClients: number;
}

// Relatórios Financeiros
export const getFinancialSummary = async (filter: ReportFilter): Promise<FinancialSummary> => {
  const response = await api.get('/reports/financial/summary', { params: filter });
  return response.data;
};

export const getRevenueTrends = async (filter: ReportFilter): Promise<RevenueTrend[]> => {
  const response = await api.get('/reports/financial/revenue-trends', { params: filter });
  return response.data;
};

export const getServicePopularity = async (filter: ReportFilter): Promise<ServicePopularity[]> => {
  const response = await api.get('/reports/services/popularity', { params: filter });
  return response.data;
};

// Relatórios de Desempenho
export const getProfessionalPerformance = async (filter: ReportFilter): Promise<ProfessionalPerformance[]> => {
  const response = await api.get('/reports/professionals/performance', { params: filter });
  return response.data;
};

// Relatórios de Clientes
export const getClientMetrics = async (filter: ReportFilter): Promise<ClientMetrics> => {
  const response = await api.get('/reports/clients/metrics', { params: filter });
  return response.data;
};

export const getClientRetention = async (filter: ReportFilter): Promise<ClientRetention[]> => {
  const response = await api.get('/reports/clients/retention', { params: filter });
  return response.data;
};

// Exportações de Relatórios
export const exportFinancialReport = async (filter: ReportFilter): Promise<Blob> => {
  const response = await api.get('/reports/export/financial', {
    params: filter,
    responseType: 'blob'
  });
  return response.data;
};

export const exportClientReport = async (filter: ReportFilter): Promise<Blob> => {
  const response = await api.get('/reports/export/clients', {
    params: filter,
    responseType: 'blob'
  });
  return response.data;
};

export const exportServiceReport = async (filter: ReportFilter): Promise<Blob> => {
  const response = await api.get('/reports/export/services', {
    params: filter,
    responseType: 'blob'
  });
  return response.data;
};
