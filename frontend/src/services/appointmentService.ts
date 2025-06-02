import api from './api';

// Interfaces para tipagem
export interface Appointment {
  id: string;
  clientId: string;
  clientName: string;
  professionalId: string;
  professionalName: string;
  service: string;
  date: string; // formato ISO
  startTime: string;
  endTime: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELED' | 'NO_SHOW';
  price: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAppointmentDto {
  clientId: string;
  professionalId: string;
  service: string;
  date: string;
  startTime: string;
  endTime: string;
  price: number;
  notes?: string;
}

export interface UpdateAppointmentDto {
  clientId?: string;
  professionalId?: string;
  service?: string;
  date?: string;
  startTime?: string;
  endTime?: string;
  price?: number;
  notes?: string;
  status?: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELED' | 'NO_SHOW';
}

export interface AppointmentsResponse {
  data: Appointment[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AppointmentResponse {
  data: Appointment;
  message: string;
}

// Função para obter todos os agendamentos com paginação
export const getAppointments = async (
  page: number = 0, 
  limit: number = 10,
  status?: string,
  clientId?: string,
  professionalId?: string,
  startDate?: string,
  endDate?: string
): Promise<AppointmentsResponse> => {
  try {
    const response = await api.get<AppointmentsResponse>('/appointments', {
      params: { 
        page, 
        size: limit,
        status,
        clientId,
        professionalId,
        startDate,
        endDate
      }
    });
    return response.data;
  } catch (error) {
    console.error('Erro ao buscar agendamentos:', error);
    // Fallback para dados simulados durante o desenvolvimento
    const mockAppointments = Array.from({ length: 10 }, (_, i) => ({
      id: `app-${i + 1}`,
      clientId: `client-${(i % 5) + 1}`,
      clientName: `Cliente ${(i % 5) + 1}`,
      professionalId: `prof-${(i % 3) + 1}`,
      professionalName: `Profissional ${(i % 3) + 1}`,
      service: `Serviço ${(i % 4) + 1}`,
      date: new Date(Date.now() + i * 86400000).toISOString().split('T')[0],
      startTime: '10:00',
      endTime: '11:00',
      status: ['SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELED', 'NO_SHOW'][i % 5] as any,
      price: 50 + (i * 10),
      notes: i % 2 === 0 ? `Observação para agendamento ${i + 1}` : undefined,
      createdAt: new Date(Date.now() - i * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - i * 43200000).toISOString(),
    }));

    return {
      data: mockAppointments,
      page: page,
      size: limit,
      totalElements: 50,
      totalPages: Math.ceil(50 / limit)
    };
  }
};

// Função para obter um agendamento específico pelo ID
export const getAppointmentById = async (id: string): Promise<Appointment> => {
  try {
    const response = await api.get<AppointmentResponse>(`/appointments/${id}`);
    return response.data.data;
  } catch (error) {
    console.error(`Erro ao buscar agendamento ${id}:`, error);
    // Fallback para dados simulados
    return {
      id,
      clientId: 'client-1',
      clientName: 'Cliente 1',
      professionalId: 'prof-1',
      professionalName: 'Profissional 1',
      service: 'Serviço Padrão',
      date: new Date().toISOString().split('T')[0],
      startTime: '10:00',
      endTime: '11:00',
      status: 'SCHEDULED',
      price: 100,
      notes: 'Observação para agendamento simulado',
      createdAt: new Date(Date.now() - 86400000).toISOString(),
      updatedAt: new Date().toISOString(),
    };
  }
};

// Função para obter agendamentos por cliente
export const getAppointmentsByClient = async (
  clientId: string,
  page: number = 0,
  limit: number = 10
): Promise<AppointmentsResponse> => {
  try {
    return await getAppointments(page, limit, undefined, clientId);
  } catch (error) {
    console.error(`Erro ao buscar agendamentos do cliente ${clientId}:`, error);
    throw error;
  }
};

// Função para criar um novo agendamento
export const createAppointment = async (
  appointmentData: CreateAppointmentDto
): Promise<Appointment> => {
  try {
    const response = await api.post<AppointmentResponse>('/appointments', appointmentData);
    return response.data.data;
  } catch (error) {
    console.error('Erro ao criar agendamento:', error);
    throw error;
  }
};

// Função para atualizar um agendamento existente
export const updateAppointment = async (
  id: string,
  appointmentData: UpdateAppointmentDto
): Promise<Appointment> => {
  try {
    const response = await api.put<AppointmentResponse>(`/appointments/${id}`, appointmentData);
    return response.data.data;
  } catch (error) {
    console.error(`Erro ao atualizar agendamento ${id}:`, error);
    throw error;
  }
};

// Função para excluir um agendamento
export const deleteAppointment = async (id: string): Promise<void> => {
  try {
    await api.delete(`/appointments/${id}`);
  } catch (error) {
    console.error(`Erro ao excluir agendamento ${id}:`, error);
    throw error;
  }
};

// Função para confirmar um agendamento
export const confirmAppointment = async (id: string): Promise<Appointment> => {
  try {
    const response = await api.patch<AppointmentResponse>(`/appointments/${id}/confirm`);
    return response.data.data;
  } catch (error) {
    console.error(`Erro ao confirmar agendamento ${id}:`, error);
    throw error;
  }
};

// Função para vincular um agendamento a uma transação de pagamento
export const linkAppointmentToPayment = async (
  appointmentId: string,
  transactionId: string
): Promise<{ success: boolean; message: string }> => {
  try {
    const response = await api.post(`/appointments/${appointmentId}/payment`, {
      transactionId
    });
    return response.data;
  } catch (error) {
    console.error(`Erro ao vincular agendamento ${appointmentId} ao pagamento:`, error);
    throw error;
  }
};

// Função para obter agendamentos pendentes de pagamento
export const getPendingPaymentAppointments = async (
  clientId?: string,
  page: number = 0,
  limit: number = 10
): Promise<AppointmentsResponse> => {
  try {
    const response = await api.get<AppointmentsResponse>('/appointments/pending-payment', {
      params: {
        clientId,
        page,
        size: limit
      }
    });
    return response.data;
  } catch (error) {
    console.error('Erro ao buscar agendamentos pendentes de pagamento:', error);
    // Fallback para desenvolvimento
    const mockAppointments = Array.from({ length: 5 }, (_, i) => ({
      id: `pending-app-${i + 1}`,
      clientId: clientId || `client-${(i % 3) + 1}`,
      clientName: `Cliente ${(i % 3) + 1}`,
      professionalId: `prof-${(i % 2) + 1}`,
      professionalName: `Profissional ${(i % 2) + 1}`,
      service: `Serviço ${(i % 3) + 1}`,
      date: new Date(Date.now() + i * 86400000).toISOString().split('T')[0],
      startTime: '14:00',
      endTime: '15:00',
      status: 'COMPLETED',
      price: 75 + (i * 15),
      notes: 'Aguardando pagamento',
      createdAt: new Date(Date.now() - i * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - i * 43200000).toISOString(),
    }));

    return {
      data: mockAppointments,
      page: page,
      size: limit,
      totalElements: 5,
      totalPages: 1
    };
  }
};

// Função para buscar o status de pagamento de um agendamento
export const getAppointmentPaymentStatus = async (appointmentId: string): Promise<{
  paid: boolean;
  transactionId?: string;
  paymentDate?: string;
  paymentMethod?: string;
  amount?: number;
}> => {
  try {
    const response = await api.get(`/appointments/${appointmentId}/payment-status`);
    return response.data;
  } catch (error) {
    console.error(`Erro ao verificar status de pagamento do agendamento ${appointmentId}:`, error);
    // Fallback para desenvolvimento
    return {
      paid: Math.random() > 0.5, // Simula 50% de chance de estar pago
      transactionId: Math.random() > 0.5 ? `tx-${Date.now()}` : undefined,
      paymentDate: Math.random() > 0.5 ? new Date().toISOString() : undefined,
      paymentMethod: Math.random() > 0.5 ? ['CREDIT_CARD', 'PIX', 'CASH'][Math.floor(Math.random() * 3)] : undefined,
      amount: Math.random() > 0.5 ? Math.round(100 + Math.random() * 200) : undefined
    };
  }
};

// Função para gerar uma transação a partir de um agendamento
export const createTransactionFromAppointment = async (
  appointmentId: string,
  additionalInfo?: {
    description?: string;
    dueDate?: string;
  }
): Promise<{ transactionId: string; paymentUrl?: string }> => {
  try {
    const response = await api.post(`/finance-service/transactions/from-appointment/${appointmentId}`, additionalInfo);
    return response.data;
  } catch (error) {
    console.error(`Erro ao criar transação para o agendamento ${appointmentId}:`, error);
    throw error;
  }
};

// Função para completar um agendamento
export const completeAppointment = async (id: string): Promise<Appointment> => {
  try {
    const response = await api.patch<AppointmentResponse>(`/appointments/${id}/complete`);
    return response.data.data;
  } catch (error) {
    console.error(`Erro ao completar agendamento ${id}:`, error);
    throw error;
  }
};

// Função para cancelar um agendamento
export const cancelAppointment = async (id: string): Promise<Appointment> => {
  try {
    const response = await api.patch<AppointmentResponse>(`/appointments/${id}/cancel`);
    return response.data.data;
  } catch (error) {
    console.error(`Erro ao cancelar agendamento ${id}:`, error);
    throw error;
  }
};

// Função para marcar um agendamento como não comparecido
export const markNoShow = async (id: string): Promise<Appointment> => {
  try {
    const response = await api.patch<AppointmentResponse>(`/appointments/${id}/no-show`);
    return response.data.data;
  } catch (error) {
    console.error(`Erro ao marcar agendamento ${id} como não comparecido:`, error);
    throw error;
  }
};
