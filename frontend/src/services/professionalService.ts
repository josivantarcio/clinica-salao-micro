import { api } from './api';

export interface Professional {
  id: number;
  name: string;
  email: string;
  phone: string;
  specialties: string[];
  imageUrl?: string;
  active: boolean;
  registrationNumber: string;
  workingHours: WorkingHours[];
}

export interface WorkingHours {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  breakStart?: string;
  breakEnd?: string;
}

export interface ProfessionalRequest {
  name: string;
  email: string;
  phone: string;
  specialties: string[];
  imageUrl?: string;
  registrationNumber: string;
  workingHours: WorkingHours[];
}

export const getProfessionals = async (): Promise<Professional[]> => {
  const response = await api.get('/professionals');
  return response.data;
};

export const getProfessionalById = async (id: number): Promise<Professional> => {
  const response = await api.get(`/professionals/${id}`);
  return response.data;
};

export const createProfessional = async (professional: ProfessionalRequest): Promise<Professional> => {
  const response = await api.post('/professionals', professional);
  return response.data;
};

export const updateProfessional = async (id: number, professional: ProfessionalRequest): Promise<Professional> => {
  const response = await api.put(`/professionals/${id}`, professional);
  return response.data;
};

export const deleteProfessional = async (id: number): Promise<void> => {
  await api.delete(`/professionals/${id}`);
};

export const getProfessionalsBySpecialty = async (specialty: string): Promise<Professional[]> => {
  const response = await api.get(`/professionals/specialty/${specialty}`);
  return response.data;
};

export const getAvailableProfessionals = async (date: string, startTime: string, endTime: string): Promise<Professional[]> => {
  const response = await api.get('/professionals/available', {
    params: { date, startTime, endTime }
  });
  return response.data;
};
